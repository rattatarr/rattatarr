import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { toast } from 'vue-sonner'
import { useQueryClient } from '@tanstack/vue-query'
import { jobKeys, movieKeys, seriesKeys, watchActivityKeys } from '@/queries/queryKeys'
import type { BackgroundJob, JobType } from '@/types'
import { JOB_TYPE } from '@/utils/enums'
import type { WebSocketMessage } from '@/composables/useJobWebSocket'

const JOB_TIMEOUT_MS = 10 * 60 * 1000

// Module-level STOMP singleton — survives component unmounts
let stompClient: Client | null = null
let connectPromise: Promise<void> | null = null

function ensureConnected(): Promise<void> {
  if (stompClient?.connected) return Promise.resolve()
  if (connectPromise) return connectPromise

  connectPromise = new Promise<void>((resolve, reject) => {
    stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws') as WebSocket,
      reconnectDelay: 5000,
      onConnect: () => resolve(),
      onStompError: (frame) => reject(new Error(frame.headers['message'] ?? 'STOMP error')),
    })
    stompClient.activate()
  }).finally(() => {
    connectPromise = null
  })

  return connectPromise
}

interface TrackedJob {
  jobId: string
  timeoutId: ReturnType<typeof setTimeout>
}

export const useJobTrackingStore = defineStore('jobTracking', () => {
  const queryClient = useQueryClient()

  const runningJobs = ref(new Map<JobType, TrackedJob>())
  // Locked immediately on button click, before the HTTP response arrives
  const pendingJobs = ref(new Set<JobType>())

  const isJellyfinSyncRunning = computed(() => runningJobs.value.has(JOB_TYPE.JELLYFIN_SYNC))
  const isJellyfinActivityPollRunning = computed(() =>
    runningJobs.value.has(JOB_TYPE.JELLYFIN_ACTIVITY_POLL),
  )
  const isImdbImportRunning = computed(() => runningJobs.value.has(JOB_TYPE.CSV_IMPORT))
  const isRadarrImportRunning = computed(() => runningJobs.value.has(JOB_TYPE.RADARR_IMPORT))
  const isRadarrRatingsRefreshRunning = computed(() =>
    runningJobs.value.has(JOB_TYPE.RADARR_RATINGS_REFRESH),
  )
  const isSonarrImportRunning = computed(() => runningJobs.value.has(JOB_TYPE.SONARR_IMPORT))
  const isAnyJobRunning = computed(() => runningJobs.value.size > 0 || pendingJobs.value.size > 0)

  function lockJob(type: JobType) {
    pendingJobs.value.add(type)
  }

  function unlockJob(type: JobType) {
    pendingJobs.value.delete(type)
  }

  function finish(type: JobType) {
    const entry = runningJobs.value.get(type)
    if (entry) {
      clearTimeout(entry.timeoutId)
      runningJobs.value.delete(type)
    }
    void queryClient.invalidateQueries({ queryKey: jobKeys.all })
    void queryClient.invalidateQueries({ queryKey: movieKeys.all })
    void queryClient.invalidateQueries({ queryKey: seriesKeys.all })
    void queryClient.invalidateQueries({ queryKey: watchActivityKeys.all })
  }

  async function startTracking(job: BackgroundJob) {
    if (!job.id || !job.type) return
    const type = job.type
    const jobId = job.id

    // HTTP succeeded — move from pending to tracked
    pendingJobs.value.delete(type)

    // Clear any stale tracker for the same type
    const existing = runningJobs.value.get(type)
    if (existing) clearTimeout(existing.timeoutId)

    const timeoutId = setTimeout(() => {
      const jobLabels: Record<JobType, string> = {
        [JOB_TYPE.JELLYFIN_SYNC]: 'Jellyfin sync',
        [JOB_TYPE.JELLYFIN_ACTIVITY_POLL]: 'Jellyfin activity poll',
        [JOB_TYPE.CSV_IMPORT]: 'CSV import',
        [JOB_TYPE.RADARR_IMPORT]: 'Radarr import',
        [JOB_TYPE.RADARR_RATINGS_REFRESH]: 'Radarr ratings refresh',
        [JOB_TYPE.SONARR_IMPORT]: 'Sonarr import',
      }
      toast.warning('Job timed out', {
        description: `${jobLabels[type]} did not finish within 10 minutes`,
      })
      finish(type)
    }, JOB_TIMEOUT_MS)

    runningJobs.value.set(type, { jobId, timeoutId })

    try {
      await ensureConnected()

      const sub = stompClient!.subscribe(`/topic/jobs/${jobId}`, (frame) => {
        try {
          const msg = JSON.parse(frame.body) as WebSocketMessage
          if (msg.message_type === 'JOB_COMPLETED') {
            toast.success('Job completed', { description: msg.payload?.message ?? undefined })
            finish(type)
            sub.unsubscribe()
          } else if (msg.message_type === 'JOB_FAILED') {
            toast.error(msg.payload?.message ?? 'Job failed')
            finish(type)
            sub.unsubscribe()
          }
        } catch {
          // ignore malformed frames
        }
      })
    } catch {
      // WS unavailable — timeout will still clean up the lock
    }
  }

  return {
    isJellyfinSyncRunning,
    isJellyfinActivityPollRunning,
    isImdbImportRunning,
    isRadarrImportRunning,
    isRadarrRatingsRefreshRunning,
    isSonarrImportRunning,
    isAnyJobRunning,
    lockJob,
    unlockJob,
    startTracking,
  }
})
