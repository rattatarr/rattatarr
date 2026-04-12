<script setup lang="ts">
  import { ref } from 'vue'
  import Button from 'primevue/button'
  import { Icon } from '@/utils/enums'
  import type { Episode } from '@/types'

  interface Props {
    title?: string
    episodeCount?: number
    airDate?: string
    posterUrl?: string
    episodes?: Episode[]
    initiallyExpanded?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    initiallyExpanded: false,
  })

  const isExpanded = ref(props.initiallyExpanded)

  const toggleExpanded = () => {
    isExpanded.value = !isExpanded.value
  }

  const formatDate = (date: string): string => {
    try {
      return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
      })
    } catch {
      return date
    }
  }
</script>

<template>
  <div class="season-dropdown">
    <div
      class="season-header"
      :style="{ backgroundImage: posterUrl ? `url(${posterUrl})` : undefined }"
      @click="toggleExpanded"
    >
      <div class="season-overlay">
        <div class="season-info">
          <h3 class="season-title">{{ title }}</h3>
          <span v-if="episodeCount" class="episode-count">{{ episodeCount }} episodes</span>
          <span v-if="airDate" class="air-date">{{ formatDate(airDate) }}</span>
        </div>
        <Button
          :icon="isExpanded ? Icon.CHEVRON_UP : Icon.CHEVRON_DOWN"
          text
          rounded
          severity="secondary"
        />
      </div>
    </div>

    <Transition name="expand">
      <div v-if="isExpanded" class="episodes-list">
        <div v-for="episode in episodes" :key="episode.id" class="episode-item">
          <div class="episode-number">{{ episode.episode }}</div>
          <div class="episode-details">
            <div class="episode-title">{{ episode.title }}</div>
            <div v-if="episode.runtimeMinutes" class="episode-runtime">
              {{ episode.runtimeMinutes }} min
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
  .season-dropdown {
    border: 1px solid var(--surface-border);
    border-radius: var(--border-radius);
    overflow: hidden;
    background: var(--surface-card);
  }

  .season-header {
    position: relative;
    min-height: 100px;
    cursor: pointer;
    transition: transform 0.2s;
    overflow: hidden;
  }

  /* Blurred backdrop layer */
  .season-header::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image: inherit;
    background-size: cover;
    background-position: center 25%;
    filter: blur(8px);
    transform: scale(1.1); /* Prevent blur edge artifacts */
    z-index: 0;
  }

  .season-header:hover {
    transform: translateY(-2px);
  }

  .season-overlay {
    position: relative;
    inset: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1.5rem;
    gap: 1rem;
    min-height: 100px;
    z-index: 1;
  }

  .season-info {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    color: white;
  }

  .season-title {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0;
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  }

  .episode-count,
  .air-date {
    font-size: 0.875rem;
    color: rgba(255, 255, 255, 0.9);
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
  }

  .episodes-list {
    padding: 1rem;
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    background: var(--surface-ground);
  }

  .episode-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0.875rem 1rem;
    border-radius: var(--border-radius);
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    transition: all 0.2s ease;
  }

  .episode-item:hover {
    background-color: var(--surface-hover);
    box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12);
    transform: translateX(4px);
    border-color: var(--primary-color);
  }

  .episode-number {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: var(--primary-color);
    color: white;
    font-weight: 600;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  .episode-details {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    min-width: 0;
    flex: 1;
  }

  .episode-title {
    font-weight: 500;
    color: var(--p-text-color);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .episode-runtime {
    font-size: 0.875rem;
    color: var(--p-text-color-secondary);
  }

  /* Expand transition */
  .expand-enter-active,
  .expand-leave-active {
    transition: all 0.3s ease;
    max-height: 2000px;
  }

  .expand-enter-from,
  .expand-leave-to {
    max-height: 0;
    opacity: 0;
  }

  @media (max-width: 768px) {
    .season-overlay {
      padding: 1rem;
    }

    .season-title {
      font-size: 1.125rem;
    }
  }
</style>
