<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import Chart from 'primevue/chart'
  import type { RatingDistribution } from '@/types'
  import { useCssColor } from '@/utils/cssColor'

  const cssColor = useCssColor(
    'var(--p-primary-color)',
    'var(--p-surface-200)',
    'var(--p-text-muted-color)',
  )

  interface Props {
    distribution: RatingDistribution[]
    distributionByInteger: RatingDistribution[]
  }

  const props = defineProps<Props>()

  type ViewMode = 'spread' | 'compact'
  const viewMode = ref<ViewMode>('spread')

  const activeDistribution = computed<RatingDistribution[]>(() =>
    viewMode.value === 'spread' ? props.distributionByInteger : props.distribution,
  )

  const chartData = computed(() => ({
    labels: activeDistribution.value.map((d) => d.range ?? ''),
    datasets: [
      {
        data: activeDistribution.value.map((d) => d.count ?? 0),
        backgroundColor: cssColor['var(--p-primary-color)'],
        borderRadius: 4,
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
            const d = activeDistribution.value[ctx.dataIndex]
            const pct = d?.percentage?.toFixed(1) ?? '0.0'
            return ` ${ctx.raw.toLocaleString()} ratings (${pct}%)`
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

  const chartHeight = computed(() => Math.max(activeDistribution.value.length * 28, 120))
</script>

<template>
  <Card class="distribution-card">
    <template #title>
      <div class="card-header">
        <span>Rating Distribution</span>
        <div class="toggle-buttons">
          <Button
            label="Spread"
            size="small"
            :severity="viewMode === 'spread' ? 'primary' : 'secondary'"
            @click="viewMode = 'spread'"
          />
          <Button
            label="Compact"
            size="small"
            :severity="viewMode === 'compact' ? 'primary' : 'secondary'"
            @click="viewMode = 'compact'"
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
  .distribution-card {
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
