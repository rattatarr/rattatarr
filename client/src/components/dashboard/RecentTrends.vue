<script setup lang="ts">
  import Card from 'primevue/card'
  import Chart from 'primevue/chart'
  import Button from 'primevue/button'
  import type { RecentTrends } from '@/types'
  import { computed, ref, watchEffect } from 'vue'
  import { useCssColor } from '@/utils/cssColor'

  const cssColor = useCssColor(
    'var(--p-primary-color)',
    'var(--p-amber-400)',
    'var(--p-surface-200)',
    'var(--p-text-muted-color)',
  )

  interface Props {
    trends: RecentTrends[]
    jellyfinTrends?: RecentTrends[]
  }

  const props = defineProps<Props>()

  type ViewMode = 'count' | 'score' | 'jellyfin'
  const viewMode = ref<ViewMode>('count')

  const hasPrimaryData = computed(() => props.trends.length > 0)
  const hasJellyfinData = computed(() => (props.jellyfinTrends?.length ?? 0) > 0)

  watchEffect(() => {
    if (!hasPrimaryData.value && hasJellyfinData.value) {
      viewMode.value = 'jellyfin'
      return
    }

    if (viewMode.value === 'jellyfin' && !hasJellyfinData.value) {
      viewMode.value = 'count'
    }
  })

  const activeTrends = computed(() =>
    viewMode.value === 'jellyfin' ? (props.jellyfinTrends ?? []) : props.trends,
  )

  function formatPeriod(period: string): string {
    switch (period) {
      case '30_DAYS':
        return '30 days'
      case '90_DAYS':
        return '90 days'
      case '365_DAYS':
        return '1 year'
      default:
        return period
    }
  }

  const chartData = computed(() => ({
    labels: activeTrends.value.map((t) => formatPeriod(t.period ?? '')),
    datasets: [
      {
        type: 'bar' as const,
        label: viewMode.value === 'jellyfin' ? 'Titles' : 'Ratings',
        data: activeTrends.value.map((t) => t.count ?? 0),
        backgroundColor: cssColor['var(--p-primary-color)'],
        borderRadius: 4,
        borderSkipped: false,
        yAxisID: 'yCount',
        order: 1,
      },
      {
        type: 'line' as const,
        label: 'Avg Rating',
        data: activeTrends.value.map((t) => t.averageRating ?? null),
        borderColor: cssColor['var(--p-amber-400)'],
        backgroundColor: 'transparent',
        pointBackgroundColor: cssColor['var(--p-amber-400)'],
        pointRadius: 5,
        pointHoverRadius: 7,
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
              if (viewMode.value === 'jellyfin') {
                return ''
              }
              return ` Avg rating: ${ctx.raw?.toFixed(2) ?? '-'}`
            }
            return ` ${viewMode.value === 'jellyfin' ? 'Titles' : 'Ratings'}: ${(ctx.raw as number).toLocaleString()}`
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

  const activeDatasets = computed(() =>
    viewMode.value === 'jellyfin' ? chartData.value.datasets.slice(0, 1) : chartData.value.datasets,
  )

  const displayChartData = computed(() => ({
    labels: chartData.value.labels,
    datasets: activeDatasets.value,
  }))
</script>

<template>
  <Card class="trends-card">
    <template #title>
      <div class="card-header">
        <span>Recent Trends</span>
        <div class="toggle-buttons">
          <Button
            label="Count"
            size="small"
            :severity="viewMode === 'count' ? 'primary' : 'secondary'"
            @click="viewMode = 'count'"
          />
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
            :disabled="!jellyfinTrends?.length"
            @click="viewMode = 'jellyfin'"
          />
        </div>
      </div>
    </template>
    <template #content>
      <Chart type="bar" :data="displayChartData" :options="chartOptions" class="trends-chart" />
    </template>
  </Card>
</template>

<style scoped>
  .trends-card {
    height: 100%;
  }

  .trends-chart {
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
