<script setup lang="ts">
  import Badge from 'primevue/badge'
  import jellyfinIcon from '@/assets/jellyfin-icon.svg'
  import { MediaSource as SearchSource, type SearchResultSource } from '@/utils/enums'

  interface Props {
    source: SearchResultSource
    compact?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    compact: false,
  })

  const badgeConfig: Record<
    SearchResultSource,
    { label: string; severity: 'secondary' | 'info' | 'primary' | 'warning'; icon: string | null }
  > = {
    [SearchSource.INTERNAL]: {
      label: 'Internal',
      severity: 'secondary',
      icon: null,
    },
    [SearchSource.JELLYFIN]: {
      label: 'Jellyfin',
      severity: 'info',
      icon: jellyfinIcon,
    },
    [SearchSource.TMDB]: {
      label: 'TMDB',
      severity: 'primary',
      icon: null,
    },
    [SearchSource.IMDB]: {
      label: 'IMDb',
      severity: 'warning',
      icon: null,
    },
  }

  const config = badgeConfig[props.source]
</script>

<template>
  <div class="source-badge" :class="{ 'source-badge--compact': compact }">
    <img v-if="config.icon" :src="config.icon" alt="" class="source-badge__icon" />
    <Badge :value="config.label" :severity="config.severity" />
  </div>
</template>

<style scoped>
  .source-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.375rem;
  }

  .source-badge--compact {
    font-size: 0.75rem;
  }

  .source-badge__icon {
    width: 16px;
    height: 16px;
    filter: brightness(0.9);
  }

  .source-badge--compact .source-badge__icon {
    width: 14px;
    height: 14px;
  }
</style>
