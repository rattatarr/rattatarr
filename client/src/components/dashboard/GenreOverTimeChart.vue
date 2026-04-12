<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import Chart from 'primevue/chart'
  import type { GenreOverTimeYear } from '@/types'
  import { useCssColor } from '@/utils/cssColor'

  const cssColor = useCssColor('var(--p-text-muted-color)', 'var(--p-surface-200)')

  interface Props {
    data: GenreOverTimeYear[]
  }

  const props = defineProps<Props>()

  type ViewMode = 'count' | 'score'
  const viewMode = ref<ViewMode>('count')

  const PALETTE = [
    '#6366f1', // indigo
    '#06b6d4', // cyan
    '#f59e0b', // amber
    '#10b981', // emerald
    '#f43f5e', // rose
    '#8b5cf6', // violet
    '#ec4899', // pink
    '#14b8a6', // teal
    '#f97316', // orange
    '#84cc16', // lime
  ]

  const sortedYears = computed(() => [...props.data].sort((a, b) => (a.year ?? 0) - (b.year ?? 0)))

  // Collect all unique genres sorted by total count descending
  const allGenres = computed<string[]>(() => {
    const totals = new Map<string, number>()
    for (const yearData of props.data) {
      for (const g of yearData.genres ?? []) {
        if (g.genreName) {
          totals.set(g.genreName, (totals.get(g.genreName) ?? 0) + (g.count ?? 0))
        }
      }
    }
    return [...totals.keys()].sort((a, b) => (totals.get(b) ?? 0) - (totals.get(a) ?? 0))
  })

  const chartData = computed(() => {
    const years = sortedYears.value
    const labels = years.map((y) => String(y.year ?? ''))

    const datasets = allGenres.value.map((genre, i) => ({
      label: genre,
      data: years.map((yearData) => {
        const entry = (yearData.genres ?? []).find((g) => g.genreName === genre)
        return viewMode.value === 'count' ? (entry?.count ?? 0) : (entry?.averageRating ?? 0)
      }),
      backgroundColor: PALETTE[i % PALETTE.length] ?? '#94a3b8',
      borderRadius: 2,
      borderSkipped: false,
    }))

    return { labels, datasets }
  })

  const chartOptions = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom' as const,
        labels: {
          color: cssColor['var(--p-text-muted-color)'],
          boxWidth: 10,
          boxHeight: 10,
          font: { size: 11 },
          padding: 12,
        },
      },
      tooltip: {
        callbacks: {
          label: (ctx: { dataset: { label?: string }; raw: number }) => {
            if (viewMode.value === 'count') {
              return ` ${ctx.dataset.label}: ${(ctx.raw as number).toLocaleString()} ratings`
            }
            return ` ${ctx.dataset.label}: avg ${(ctx.raw as number).toFixed(2)}`
          },
        },
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: cssColor['var(--p-text-muted-color)'], font: { size: 11 } },
      },
      y: {
        grid: { color: cssColor['var(--p-surface-200)'] },
        ticks: {
          color: cssColor['var(--p-text-muted-color)'],
          font: { size: 11 },
          callback: (value: number | string) =>
            viewMode.value === 'score' ? Number(value).toFixed(1) : Number(value).toLocaleString(),
        },
        min: viewMode.value === 'score' ? 0 : undefined,
        max: viewMode.value === 'score' ? 10 : undefined,
      },
    },
  }))
</script>

<template>
  <Card class="got-card">
    <template #title>
      <div class="card-header">
        <span>Genre Over Time</span>
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
        </div>
      </div>
    </template>

    <template #content>
      <Chart type="bar" :data="chartData" :options="chartOptions" class="got-chart" />
    </template>
  </Card>
</template>

<style scoped>
  .got-card {
    width: 100%;
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

  .got-chart {
    height: 320px !important;
  }
</style>
