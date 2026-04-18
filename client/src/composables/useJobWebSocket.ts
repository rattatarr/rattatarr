import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { BackgroundJob } from '@/types'

export type WebSocketMessageType =
  | 'JOB_STARTED'
  | 'JOB_PROGRESS'
  | 'JOB_COMPLETED'
  | 'JOB_FAILED'
  | 'KEEP_ALIVE'

export interface WebSocketMessage {
  message_type: WebSocketMessageType
  payload?: BackgroundJob
}

type JobEventCallback = (message: WebSocketMessage) => void

const WS_URL = '/ws'
const KEEPALIVE_INTERVAL_MS = 30_000

export function useJobWebSocket() {
  const connected = ref(false)
  let client: Client | null = null
  let keepAliveTimer: ReturnType<typeof setInterval> | null = null

  function connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      client = new Client({
        webSocketFactory: () => new SockJS(WS_URL) as WebSocket,
        reconnectDelay: 5000,
        onConnect: () => {
          connected.value = true
          keepAliveTimer = setInterval(() => {
            client?.publish({ destination: '/app/keepalive', body: '{}' })
          }, KEEPALIVE_INTERVAL_MS)
          resolve()
        },
        onDisconnect: () => {
          connected.value = false
        },
        onStompError: (frame) => {
          reject(new Error(frame.headers['message'] ?? 'STOMP error'))
        },
      })
      client.activate()
    })
  }

  function subscribeToJob(jobId: string, onEvent: JobEventCallback): () => void {
    if (!client?.connected) return () => {}
    const sub = client.subscribe(`/topic/jobs/${jobId}`, (frame) => {
      try {
        const msg = JSON.parse(frame.body) as WebSocketMessage
        onEvent(msg)
      } catch {
        // ignore malformed frames
      }
    })
    return () => sub.unsubscribe()
  }

  function disconnect() {
    if (keepAliveTimer !== null) {
      clearInterval(keepAliveTimer)
      keepAliveTimer = null
    }
    client?.deactivate()
    client = null
    connected.value = false
  }

  onUnmounted(disconnect)

  return { connected, connect, subscribeToJob, disconnect }
}
