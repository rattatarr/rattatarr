<script setup lang="ts">
  import { computed } from 'vue'
  import MediaPosterCard from '@/components/media/MediaPosterCard.vue'
  import { Icon } from '@/utils/enums'
  import type { WatchEventResponse } from '@/types'

  const props = defineProps<{ event: WatchEventResponse }>()
  const emit = defineEmits<{ click: [] }>()

  const posterUrl = computed(
    () => props.event.movie?.metadata?.posterImageUrl ?? props.event.show?.metadata?.posterImageUrl,
  )

  const title = computed(() => props.event.movie?.title ?? props.event.show?.title ?? 'Unknown')

  const subtitle = computed(() => {
    if (props.event.show) {
      const parts: string[] = []
      if (props.event.seasonNumber != null)
        parts.push(`S${String(props.event.seasonNumber).padStart(2, '0')}`)
      if (props.event.episodeNumber != null)
        parts.push(`E${String(props.event.episodeNumber).padStart(2, '0')}`)
      const prefix = parts.join('')
      return prefix
        ? props.event.episodeTitle
          ? `${prefix} · ${props.event.episodeTitle}`
          : prefix
        : (props.event.episodeTitle ?? '')
    }
    return ''
  })

  const eventTypeBadge = computed(() => {
    if (props.event.eventType === 'COMPLETE') return 'Watched'
    if (props.event.eventType === 'PROGRESS') return 'In Progress'
    return 'Started'
  })

  function formatDate(iso?: string): string {
    if (!iso) return ''
    return new Date(iso).toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  }
</script>

<template>
  <MediaPosterCard
    :title="title"
    :subtitle="subtitle"
    :poster-url="posterUrl"
    @click="emit('click')"
  >
    <template #overlay>
      <div class="event-overlay">
        <span class="event-type-badge">{{ eventTypeBadge }}</span>
      </div>
    </template>
    <template #footer>
      <div class="event-date">
        <i :class="Icon.CLOCK" />
        {{ formatDate(event.watchedAt) }}
      </div>
    </template>
  </MediaPosterCard>
</template>

<style scoped>
  .event-overlay {
    position: absolute;
    bottom: 8px;
    right: 8px;
    z-index: 5;
  }

  .event-type-badge {
    padding: 3px 8px;
    border-radius: 12px;
    background: rgba(0, 0, 0, 0.7);
    backdrop-filter: blur(6px);
    color: #fff;
    font-size: 0.7rem;
    font-weight: 600;
    letter-spacing: 0.02em;
  }

  .event-date {
    padding: 0 12px 10px;
    font-size: 0.75rem;
    color: var(--p-text-secondary-color);
    display: flex;
    align-items: center;
    gap: 0.3rem;
  }
</style>
