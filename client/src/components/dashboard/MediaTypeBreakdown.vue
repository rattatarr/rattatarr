<script setup lang="ts">
  import { ref, computed, watchEffect } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import Chart from 'primevue/chart'
  import type { MediaTypeBreakdown } from '@/types'

  interface Props {
    breakdown: MediaTypeBreakdown[]
    jellyfinBreakdown?: MediaTypeBreakdown[]
  }

  const props = defineProps<Props>()

  type ViewMode = 'rated' | 'all' | 'jellyfin'
  const viewMode = ref<ViewMode>('rated')

  const hasPrimaryData = computed(() => props.breakdown.length > 0)
  const hasJellyfinData = computed(() => (props.jellyfinBreakdown?.length ?? 0) > 0)

  watchEffect(() => {
    if (!hasPrimaryData.value && hasJellyfinData.value) {
      viewMode.value = 'jellyfin'
      return
    }

    if (viewMode.value === 'jellyfin' && !hasJellyfinData.value) {
      viewMode.value = 'rated'
    }
  })

  const activeBreakdown = computed(() =>
    viewMode.value === 'jellyfin' ? (props.jellyfinBreakdown ?? []) : props.breakdown,
  )

  const SLICE_COLORS = [
    '#6366f1', // indigo  – Movie
    '#06b6d4', // cyan    – Series
    '#f59e0b', // amber
    '#10b981', // emerald
    '#f43f5e', // rose
  ]

  interface SliceData {
    label: string
    count: number
    totalCount: number
    percentage: number
    allPercentage: number
    averageRating: number
    color: string
  }

  const slices = computed<SliceData[]>(() => {
    const allTotal = activeBreakdown.value.reduce((s, b) => s + (b.totalCount ?? 0), 0)
    return activeBreakdown.value.map((b, i) => ({
      label: b.mediaType ?? `Type ${i + 1}`,
      count: b.count ?? 0,
      totalCount: b.totalCount ?? 0,
      percentage: b.percentage ?? 0,
      allPercentage: allTotal > 0 ? ((b.totalCount ?? 0) / allTotal) * 100 : 0,
      averageRating: b.averageRating ?? 0,
      color: SLICE_COLORS[i] ?? '#94a3b8',
    }))
  })

  const totalRated = computed(() => slices.value.reduce((s, b) => s + b.count, 0))
  const totalAll = computed(() => slices.value.reduce((s, b) => s + b.totalCount, 0))
  const overallAvg = computed(() => {
    const weightedSum = slices.value.reduce((s, b) => s + b.averageRating * b.count, 0)
    return totalRated.value > 0 ? weightedSum / totalRated.value : 0
  })

  const centerTitle = computed(() =>
    viewMode.value === 'rated'
      ? `${totalRated.value.toLocaleString()}`
      : `${totalAll.value.toLocaleString()}`,
  )

  const centerSubtitle = computed(() =>
    viewMode.value === 'rated' ? `avg ${overallAvg.value.toFixed(2)}` : 'titles',
  )

  const chartData = computed(() => ({
    labels: slices.value.map((s) => s.label),
    datasets: [
      {
        data: slices.value.map((s) => (viewMode.value === 'rated' ? s.count : s.totalCount)),
        backgroundColor: slices.value.map((s) => s.color),
        borderWidth: 0,
        hoverOffset: 8,
      },
    ],
  }))

  const chartOptions = computed(() => ({
    responsive: true,
    maintainAspectRatio: false,
    cutout: '60%',
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        callbacks: {
          label: (ctx: { dataIndex: number; formattedValue: string }) => {
            const s = slices.value[ctx.dataIndex]
            if (!s) return ''
            const pct = viewMode.value === 'rated' ? s.percentage : s.allPercentage
            if (viewMode.value === 'rated') {
              return ` ${s.label}: ${pct.toFixed(1)}% (${ctx.formattedValue} titles, avg ${s.averageRating.toFixed(2)})`
            }
            return ` ${s.label}: ${pct.toFixed(1)}% (${ctx.formattedValue} titles)`
          },
        },
      },
    },
  }))
</script>

<template>
  <Card class="breakdown-card">
    <template #title>
      <div class="card-header">
        <span>Media Type Breakdown</span>
        <div class="toggle-buttons">
          <Button
            label="Rated"
            size="small"
            :severity="viewMode === 'rated' ? 'primary' : 'secondary'"
            @click="viewMode = 'rated'"
          />
          <Button
            label="All"
            size="small"
            :severity="viewMode === 'all' ? 'primary' : 'secondary'"
            @click="viewMode = 'all'"
          />
          <Button
            label="Jellyfin"
            size="small"
            :severity="viewMode === 'jellyfin' ? 'primary' : 'secondary'"
            :disabled="!jellyfinBreakdown?.length"
            @click="viewMode = 'jellyfin'"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div class="chart-area">
        <!-- Doughnut chart -->
        <div class="chart-container">
          <Chart type="doughnut" :data="chartData" :options="chartOptions" class="donut-chart" />
          <div class="donut-center">
            <span class="center-value">{{ centerTitle }}</span>
            <span class="center-sub">{{ centerSubtitle }}</span>
          </div>
        </div>

        <!-- Legend -->
        <div class="legend">
          <div v-for="slice in slices" :key="slice.label" class="legend-item">
            <span class="legend-swatch" :style="{ background: slice.color }" />
            <div class="legend-text">
              <span class="legend-name">{{ slice.label }}</span>
              <span class="legend-count">
                {{ (viewMode === 'rated' ? slice.count : slice.totalCount).toLocaleString() }}
                titles
              </span>
              <span v-if="viewMode === 'rated'" class="legend-avg">
                avg {{ slice.averageRating.toFixed(2) }}
              </span>
              <span class="legend-pct">
                {{ (viewMode === 'rated' ? slice.percentage : slice.allPercentage).toFixed(1) }}%
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
  .breakdown-card {
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

  .chart-area {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 2rem;
    padding: 0.5rem 0;
    flex-wrap: wrap;
  }

  .chart-container {
    position: relative;
    flex: 0 1 260px;
    min-width: 180px;
    max-width: 260px;
  }

  .donut-chart {
    width: 100% !important;
    height: 260px !important;
  }

  .donut-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    pointer-events: none;
  }

  .center-value {
    display: block;
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--p-text-color);
    line-height: 1.2;
  }

  .center-sub {
    display: block;
    font-size: 0.8rem;
    color: var(--p-text-muted-color);
    margin-top: 0.1rem;
  }

  @media (max-width: 480px) {
    .chart-area {
      flex-direction: column;
      align-items: center;
    }

    .chart-container {
      flex: none;
      width: 80%;
      max-width: 240px;
    }

    .legend {
      flex-direction: row;
      flex-wrap: wrap;
      justify-content: center;
      gap: 1rem;
      min-width: unset;
    }
  }

  /* Legend */
  .legend {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    min-width: 160px;
  }

  .legend-item {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .legend-swatch {
    width: 16px;
    height: 16px;
    border-radius: 4px;
    flex-shrink: 0;
    margin-top: 3px;
  }

  .legend-text {
    display: flex;
    flex-direction: column;
    gap: 0.15rem;
  }

  .legend-name {
    font-size: 1rem;
    font-weight: 700;
    color: var(--p-text-color);
    text-transform: capitalize;
  }

  .legend-count {
    font-size: 0.82rem;
    color: var(--p-text-muted-color);
  }

  .legend-avg {
    font-size: 0.82rem;
    color: var(--p-text-muted-color);
  }

  .legend-pct {
    font-size: 0.95rem;
    font-weight: 700;
    color: var(--p-primary-color);
  }
</style>
