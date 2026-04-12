<script setup lang="ts">
  import Card from 'primevue/card'
  import Chart from 'primevue/chart'
  import type { RecentTrends } from '@/types'
  import { computed } from 'vue'
  import { useCssColor } from '@/utils/cssColor'

  const cssColor = useCssColor(
    'var(--p-primary-color)',
    'var(--p-amber-400)',
    'var(--p-surface-200)',
    'var(--p-text-muted-color)',
  )

  interface Props {
    trends: RecentTrends[]
  }

  const props = defineProps<Props>()

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
    labels: props.trends.map((t) => formatPeriod(t.period ?? '')),
    datasets: [
      {
        type: 'bar' as const,
        label: 'Ratings',
        data: props.trends.map((t) => t.count ?? 0),
        backgroundColor: cssColor['var(--p-primary-color)'],
        borderRadius: 4,
        borderSkipped: false,
        yAxisID: 'yCount',
        order: 1,
      },
      {
        type: 'line' as const,
        label: 'Avg Rating',
        data: props.trends.map((t) => t.averageRating ?? null),
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
              return ` Avg rating: ${ctx.raw?.toFixed(2) ?? '-'}`
            }
            return ` Ratings: ${(ctx.raw as number).toLocaleString()}`
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
        ticks: {
          color: cssColor['var(--p-amber-400)'],
          font: { size: 11 },
          stepSize: 2,
        },
      },
    },
  }))
</script>

<template>
  <Card class="trends-card">
    <template #title>Recent Trends</template>
    <template #content>
      <Chart type="bar" :data="chartData" :options="chartOptions" class="trends-chart" />
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
</style>
