<script setup lang="ts">
  import Card from 'primevue/card'
  import Chart from 'primevue/chart'
  import Button from 'primevue/button'
  import type { DayOfWeekActivity } from '@/types'
  import { computed, ref, watchEffect } from 'vue'
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

  type ViewMode = 'count' | 'jellyfin'
  const viewMode = ref<ViewMode>('count')

  const hasPrimaryData = computed(() => props.activity.length > 0)
  const hasJellyfinData = computed(() => (props.jellyfinActivity?.length ?? 0) > 0)

  watchEffect(() => {
    if (!hasPrimaryData.value && hasJellyfinData.value) {
      viewMode.value = 'jellyfin'
      return
    }

    if (viewMode.value === 'jellyfin' && !hasJellyfinData.value) {
      viewMode.value = 'count'
    }
  })

  const activeActivity = computed(() =>
    viewMode.value === 'jellyfin' ? (props.jellyfinActivity ?? []) : props.activity,
  )

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
          label: (ctx: { raw: number }) =>
            ` ${ctx.raw.toLocaleString()} ${viewMode.value === 'jellyfin' ? 'titles' : 'ratings'}`,
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
    <template #title>
      <div class="card-header">
        <span>Day of Week Activity</span>
        <div class="toggle-buttons">
          <Button
            label="Count"
            size="small"
            :severity="viewMode === 'count' ? 'primary' : 'secondary'"
            @click="viewMode = 'count'"
          />
          <Button
            label="Jellyfin"
            size="small"
            :severity="viewMode === 'jellyfin' ? 'primary' : 'secondary'"
            :disabled="!jellyfinActivity?.length"
            @click="viewMode = 'jellyfin'"
          />
        </div>
      </div>
    </template>
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
