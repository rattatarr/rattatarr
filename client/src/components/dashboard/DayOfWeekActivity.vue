<script setup lang="ts">
  import Card from 'primevue/card'
  import Chart from 'primevue/chart'
  import type { DayOfWeekActivity } from '@/types'
  import { computed } from 'vue'
  import { useCssColor } from '@/utils/cssColor'

  const cssColor = useCssColor(
    'var(--p-cyan-500)',
    'var(--p-surface-200)',
    'var(--p-text-muted-color)',
  )

  interface Props {
    activity: DayOfWeekActivity[]
  }

  const props = defineProps<Props>()

  const chartData = computed(() => ({
    labels: props.activity.map((d) => (d.dayOfWeek ?? '').slice(0, 3)),
    datasets: [
      {
        data: props.activity.map((d) => d.count ?? 0),
        backgroundColor: cssColor['var(--p-cyan-500)'],
        borderRadius: 4,
        borderSkipped: false,
      },
    ],
  }))

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (ctx: { raw: number }) => ` ${ctx.raw.toLocaleString()} ratings`,
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
        ticks: { color: cssColor['var(--p-text-muted-color)'], font: { size: 11 } },
      },
    },
  }
</script>

<template>
  <Card class="dow-card">
    <template #title>Day of Week Activity</template>
    <template #content>
      <Chart type="bar" :data="chartData" :options="chartOptions" class="dow-chart" />
    </template>
  </Card>
</template>

<style scoped>
  .dow-card {
    height: 100%;
  }

  .dow-chart {
    height: 220px !important;
  }
</style>
