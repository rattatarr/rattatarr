<script setup lang="ts">
  import Card from 'primevue/card'
  import Chart from 'primevue/chart'
  import Button from 'primevue/button'
  import type { DecadeStat } from '@/types'
  import { computed, ref, watchEffect } from 'vue'
  import { useCssColor } from '@/utils/cssColor'

  const cssColor = useCssColor(
    'var(--p-primary-color)',
    'var(--p-amber-400)',
    'var(--p-surface-200)',
    'var(--p-text-muted-color)',
  )

  interface Props {
    decades: DecadeStat[]
    jellyfinDecades?: DecadeStat[]
  }

  const props = defineProps<Props>()

  type ViewMode = 'score' | 'jellyfin'
  const viewMode = ref<ViewMode>('score')

  const hasPrimaryData = computed(() => props.decades.length > 0)
  const hasJellyfinData = computed(() => (props.jellyfinDecades?.length ?? 0) > 0)

  watchEffect(() => {
    if (!hasPrimaryData.value && hasJellyfinData.value) {
      viewMode.value = 'jellyfin'
      return
    }

    if (viewMode.value === 'jellyfin' && !hasJellyfinData.value) {
      viewMode.value = 'score'
    }
  })

  const activeDecades = computed(() =>
    viewMode.value === 'jellyfin' ? (props.jellyfinDecades ?? []) : props.decades,
  )

  const chartData = computed(() => ({
    labels: activeDecades.value.map((d) => `${d.decade}s`),
    datasets: [
      {
        type: 'bar' as const,
        label: 'Titles',
        data: activeDecades.value.map((d) => d.count ?? 0),
        backgroundColor: cssColor['var(--p-primary-color)'],
        borderRadius: 4,
        borderSkipped: false,
        yAxisID: 'yCount',
        order: 1,
      },
      {
        type: 'line' as const,
        label: 'Avg Rating',
        data: activeDecades.value.map((d) => d.averageRating ?? null),
        borderColor: cssColor['var(--p-amber-400)'],
        backgroundColor: 'transparent',
        pointBackgroundColor: cssColor['var(--p-amber-400)'],
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 2,
        tension: 0.3,
        yAxisID: 'yRating',
        order: 0,
      },
    ],
  }))

  const chartOptions = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top' as const,
        labels: {
          color: cssColor['var(--p-text-muted-color)'],
          boxWidth: 10,
          boxHeight: 10,
          font: { size: 11 },
          padding: 10,
        },
      },
      tooltip: {
        callbacks: {
          label: (ctx: { dataset: { label?: string }; raw: number | null }) => {
            if (ctx.dataset.label === 'Avg Rating') {
              return ` Avg rating: ${ctx.raw?.toFixed(2) ?? '-'}`
            }
            return ` Titles: ${(ctx.raw as number).toLocaleString()}`
          },
        },
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: cssColor['var(--p-text-muted-color)'], font: { size: 11 } },
      },
      yCount: {
        type: 'linear' as const,
        position: 'left' as const,
        grid: { color: cssColor['var(--p-surface-200)'] },
        ticks: { color: cssColor['var(--p-text-muted-color)'], font: { size: 11 } },
      },
      yRating: {
        type: 'linear' as const,
        position: 'right' as const,
        min: 0,
        max: 10,
        grid: { display: false },
        display: viewMode.value !== 'jellyfin',
        ticks: {
          color: cssColor['var(--p-amber-400)'],
          font: { size: 11 },
          stepSize: 2,
        },
      },
    },
  }))

  const displayChartData = computed(() => ({
    labels: chartData.value.labels,
    datasets:
      viewMode.value === 'jellyfin'
        ? chartData.value.datasets.slice(0, 1)
        : chartData.value.datasets,
  }))
</script>

<template>
  <Card class="decades-card">
    <template #title>
      <div class="card-header">
        <span>Decade Preferences</span>
        <div class="toggle-buttons">
          <Button
            label="Score"
            size="small"
            :severity="viewMode === 'score' ? 'primary' : 'secondary'"
            @click="viewMode = 'score'"
          />
          <Button
            label="Jellyfin"
            size="small"
            :severity="viewMode === 'jellyfin' ? 'primary' : 'secondary'"
            :disabled="!jellyfinDecades?.length"
            @click="viewMode = 'jellyfin'"
          />
        </div>
      </div>
    </template>
    <template #content>
      <Chart type="bar" :data="displayChartData" :options="chartOptions" class="decades-chart" />
    </template>
  </Card>
</template>

<style scoped>
  .decades-card {
    height: 100%;
  }

  .decades-chart {
    height: 220px !important;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    flex-wrap: wrap;
  }

  .toggle-buttons {
    display: flex;
    gap: 0.25rem;
  }
</style>
