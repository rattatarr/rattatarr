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
    jellyfinActivity?: DayOfWeekActivity[]
  }

  const props = defineProps<Props>()

  const activeActivity = computed(() => props.jellyfinActivity ?? [])
  const hasData = computed(() => activeActivity.value.length > 0)

  const chartData = computed(() => ({
    labels: activeActivity.value.map((d) => (d.dayOfWeek ?? '').slice(0, 3)),
    datasets: [
      {
        data: activeActivity.value.map((d) => d.count ?? 0),
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
          label: (ctx: { raw: number }) => ` ${ctx.raw.toLocaleString()} titles`,
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
      <Chart
        v-if="hasData"
        type="bar"
        :data="chartData"
        :options="chartOptions"
        class="dow-chart"
      />
      <p v-else class="no-data">No watch activity data available.</p>
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

  .no-data {
    color: var(--p-text-muted-color);
    font-size: 0.875rem;
    text-align: center;
    padding: 2rem 0;
    margin: 0;
  }
</style>
