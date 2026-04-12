<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import Chart from 'primevue/chart'
  import type { GenreStat } from '@/types'
  import { useCssColor } from '@/utils/cssColor'

  const cssColor = useCssColor(
    'var(--p-cyan-500)',
    'var(--p-surface-200)',
    'var(--p-text-muted-color)',
  )

  interface Props {
    byCount: GenreStat[]
    byScore: GenreStat[]
  }

  const props = defineProps<Props>()

  type ViewMode = 'count' | 'score'
  const viewMode = ref<ViewMode>('count')

  const activeGenres = computed<GenreStat[]>(() =>
    viewMode.value === 'count' ? props.byCount : props.byScore,
  )

  const chartData = computed(() => ({
    labels: activeGenres.value.map((g) => g.genreName ?? ''),
    datasets: [
      {
        data: activeGenres.value.map((g) =>
          viewMode.value === 'count' ? (g.count ?? 0) : (g.averageRating ?? 0),
        ),
        backgroundColor: cssColor['var(--p-cyan-500)'],
        borderRadius: 3,
        borderSkipped: false,
      },
    ],
  }))

  const chartOptions = computed(() => ({
    indexAxis: 'y' as const,
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx: { raw: number; dataIndex: number }) => {
            const g = activeGenres.value[ctx.dataIndex]
            if (!g) return ''
            if (viewMode.value === 'count') {
              return ` ${ctx.raw.toLocaleString()} titles (avg ${g.averageRating?.toFixed(1)})`
            }
            return ` avg ${(ctx.raw as number).toFixed(2)} (${g.count} titles)`
          },
        },
      },
    },
    scales: {
      x: {
        grid: { color: cssColor['var(--p-surface-200)'] },
        ticks: { color: cssColor['var(--p-text-muted-color)'], font: { size: 11 } },
      },
      y: {
        grid: { display: false },
        ticks: { color: cssColor['var(--p-text-muted-color)'], font: { size: 11 } },
      },
    },
  }))

  const chartHeight = computed(() => Math.max(activeGenres.value.length * 28, 120))
</script>

<template>
  <Card class="genres-card">
    <template #title>
      <div class="card-header">
        <span>Top Genres</span>
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
      <Chart
        type="bar"
        :data="chartData"
        :options="chartOptions"
        :style="{ height: `${chartHeight}px` }"
      />
    </template>
  </Card>
</template>

<style scoped>
  .genres-card {
    height: 100%;
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
