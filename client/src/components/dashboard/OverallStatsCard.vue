<script setup lang="ts">
  import Card from 'primevue/card'
  import type { OverallStats, RuntimeStats, RatingConsistency } from '@/types'

  interface Props {
    overallStats: OverallStats
    runtimeStats?: RuntimeStats
    jellyfinRuntimeStats?: RuntimeStats
    ratingConsistency?: RatingConsistency
  }

  defineProps<Props>()

  function formatRuntime(minutes: number): string {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    if (hours === 0) return `${mins}m`
    if (mins === 0) return `${hours}h`
    return `${hours}h ${mins}m`
  }
</script>

<template>
  <div class="stats-grid">
    <Card class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Total Ratings</span>
          <span class="stat-value">{{ overallStats.totalRatings?.toLocaleString() ?? '—' }}</span>
        </div>
      </template>
    </Card>

    <Card class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Total Media Items</span>
          <span class="stat-value">{{ overallStats.totalItems?.toLocaleString() ?? '—' }}</span>
        </div>
      </template>
    </Card>

    <Card class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Average Rating</span>
          <span class="stat-value">{{
            overallStats.averageRating != null ? overallStats.averageRating.toFixed(2) : '—'
          }}</span>
        </div>
      </template>
    </Card>

    <Card class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Rating Range</span>
          <span class="stat-value"
            >{{ overallStats.minRating ?? '—' }} – {{ overallStats.maxRating ?? '—' }}</span
          >
        </div>
      </template>
    </Card>

    <Card v-if="runtimeStats" class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Avg Runtime</span>
          <span class="stat-value">{{
            runtimeStats.averageRuntime != null ? formatRuntime(runtimeStats.averageRuntime) : '—'
          }}</span>
        </div>
      </template>
    </Card>

    <Card v-if="jellyfinRuntimeStats" class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Jellyfin Total Watch Time</span>
          <span class="stat-value">{{
            jellyfinRuntimeStats.totalRuntime != null
              ? `${Math.round(jellyfinRuntimeStats.totalRuntime / 60).toLocaleString()}h`
              : '—'
          }}</span>
        </div>
      </template>
    </Card>

    <Card v-if="runtimeStats" class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Rated Total Watch Time</span>
          <span class="stat-value">{{
            runtimeStats.totalRuntime != null
              ? `${Math.round(runtimeStats.totalRuntime / 60).toLocaleString()}h`
              : '—'
          }}</span>
        </div>
      </template>
    </Card>

    <Card v-if="ratingConsistency" class="stat-card">
      <template #content>
        <div class="stat-content">
          <span class="stat-label">Rating Style</span>
          <span class="stat-value consistency">{{
            ratingConsistency.consistencyLevel ?? '—'
          }}</span>
        </div>
      </template>
    </Card>
  </div>
</template>

<style scoped>
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .stat-card :deep(.p-card-body) {
    padding: 1rem;
  }

  .stat-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.25rem;
    text-align: center;
  }

  .stat-label {
    font-size: 0.75rem;
    color: var(--p-text-muted-color);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .stat-value {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--p-primary-color);
  }

  .stat-value.consistency {
    font-size: 1rem;
    text-transform: capitalize;
    color: var(--p-green-500);
  }
</style>
