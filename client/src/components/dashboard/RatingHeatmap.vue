<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import type { RatingHeatmapYear } from '@/types'

  interface Props {
    heatmap: RatingHeatmapYear[]
  }

  const props = defineProps<Props>()

  // SVG cell geometry (fixed logical pixels — viewBox handles scaling)
  const CELL = 11
  const GAP = 2
  const STEP = CELL + GAP
  const DOW_COL = 28 // width reserved for day-of-week labels
  const MONTH_ROW = 16 // height reserved for month labels
  const SVG_HEIGHT = MONTH_ROW + 7 * STEP - GAP // total SVG height

  interface DayCell {
    date: string
    count: number
  }

  interface WeekCol {
    weekIndex: number
    days: (DayCell | null)[]
    monthLabel: string | null
  }

  interface YearData {
    year: number
    weeks: WeekCol[]
    maxCount: number
  }

  const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
  const MONTH_NAMES = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec',
  ]

  function isoDate(d: Date): string {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }

  function buildYearGrid(year: number, days: { date?: string; count?: number }[]): YearData {
    const countMap = new Map<string, number>()
    for (const d of days) {
      if (d.date) countMap.set(d.date, d.count ?? 0)
    }

    const jan1 = new Date(year, 0, 1)
    const startDow = jan1.getDay()
    const gridStart = new Date(year, 0, 1 - startDow)

    const dec31 = new Date(year, 11, 31)
    const endDow = dec31.getDay()
    const gridEnd = new Date(year, 11, 31 + (6 - endDow))

    const seenMonths = new Set<number>()

    const weeks: WeekCol[] = []
    const weekStart = new Date(gridStart)
    let weekIndex = 0

    while (weekStart <= gridEnd) {
      const weekDays: (DayCell | null)[] = []
      let firstMonthThisWeek: number | null = null

      for (let dow = 0; dow < 7; dow++) {
        const d = new Date(weekStart)
        d.setDate(d.getDate() + dow)

        if (d.getFullYear() === year) {
          const iso = isoDate(d)
          weekDays.push({ date: iso, count: countMap.get(iso) ?? 0 })

          const m = d.getMonth()
          if (!seenMonths.has(m)) {
            seenMonths.add(m)
            if (firstMonthThisWeek === null) firstMonthThisWeek = m
          }
        } else {
          weekDays.push(null)
        }
      }

      weeks.push({
        weekIndex,
        days: weekDays,
        monthLabel: firstMonthThisWeek !== null ? (MONTH_NAMES[firstMonthThisWeek] ?? null) : null,
      })

      weekStart.setDate(weekStart.getDate() + 7)
      weekIndex++
    }

    const maxCount = Math.max(...[...countMap.values()], 1)
    return { year, weeks, maxCount }
  }

  const allYearGrids = computed<YearData[]>(() =>
    [...props.heatmap]
      .sort((a, b) => (b.year ?? 0) - (a.year ?? 0))
      .map((y) => buildYearGrid(y.year ?? 0, y.days ?? [])),
  )

  const availableYears = computed(() => allYearGrids.value.map((yd) => yd.year))
  const selectedYear = ref<number | null>(null)

  const selectedYearGrid = computed<YearData | null>(() => {
    const year = selectedYear.value ?? availableYears.value[0] ?? null
    if (year === null) return null
    return allYearGrids.value.find((yd) => yd.year === year) ?? null
  })

  watch(
    () => props.heatmap,
    () => {
      selectedYear.value = null
    },
    { deep: false },
  )

  function selectYear(year: number) {
    selectedYear.value = year
  }

  function heatColor(count: number, maxCount: number): string {
    if (count === 0) return 'var(--p-surface-300)'
    const ratio = count / maxCount
    if (ratio <= 0.25) return 'var(--p-green-800, #166534)'
    if (ratio <= 0.5) return 'var(--p-green-600, #16a34a)'
    if (ratio <= 0.75) return 'var(--p-green-400, #4ade80)'
    return 'var(--p-green-300, #86efac)'
  }

  function heatClass(count: number, maxCount: number): string {
    if (count === 0) return 'heat-0'
    const ratio = count / maxCount
    if (ratio <= 0.25) return 'heat-1'
    if (ratio <= 0.5) return 'heat-2'
    if (ratio <= 0.75) return 'heat-3'
    return 'heat-4'
  }

  function tooltipText(cell: DayCell): string {
    return cell.count === 0
      ? `${cell.date}: no ratings`
      : `${cell.date}: ${cell.count} rating${cell.count !== 1 ? 's' : ''}`
  }

  // SVG viewBox width = DOW column + all week columns
  const svgWidth = computed(() => {
    const cols = selectedYearGrid.value?.weeks.length ?? 53
    return DOW_COL + cols * STEP - GAP
  })

  const viewBox = computed(() => `0 0 ${svgWidth.value} ${SVG_HEIGHT}`)

  // X position (left edge) for a given week column
  function weekX(weekIndex: number): number {
    return DOW_COL + weekIndex * STEP
  }

  // Y position (top edge) for a given day row (0=Sun … 6=Sat)
  function dayY(dow: number): number {
    return MONTH_ROW + dow * STEP
  }
</script>

<template>
  <Card class="heatmap-card">
    <template #title>
      <div class="heatmap-title-row">
        <span>Rating Heatmap</span>
        <div class="year-chips">
          <Button
            v-for="y in availableYears"
            :key="y"
            :label="String(y)"
            size="small"
            :severity="(selectedYear ?? availableYears[0]) === y ? 'primary' : 'secondary'"
            @click="selectYear(y)"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div v-if="selectedYearGrid" class="heatmap-container">
        <!-- SVG scales to container width via width="100%", height auto via viewBox -->
        <svg
          :viewBox="viewBox"
          width="100%"
          :height="undefined"
          class="heatmap-svg"
          xmlns="http://www.w3.org/2000/svg"
        >
          <!-- Day-of-week labels (every other row) -->
          <text
            v-for="(name, i) in DAY_NAMES"
            :key="`dow-${i}`"
            :x="DOW_COL - 4"
            :y="dayY(i) + CELL / 2 + 3"
            class="svg-label dow-label"
            text-anchor="end"
            :visibility="i % 2 === 0 ? 'hidden' : 'visible'"
          >
            {{ name }}
          </text>

          <!-- Month labels -->
          <text
            v-for="week in selectedYearGrid.weeks"
            :key="`ml-${week.weekIndex}`"
            :x="weekX(week.weekIndex)"
            :y="MONTH_ROW - 4"
            class="svg-label"
            text-anchor="start"
          >
            {{ week.monthLabel ?? '' }}
          </text>

          <!-- Day cells -->
          <template v-for="week in selectedYearGrid.weeks" :key="`wk-${week.weekIndex}`">
            <rect
              v-for="(cell, di) in week.days"
              :key="di"
              :x="weekX(week.weekIndex)"
              :y="dayY(di)"
              :width="CELL"
              :height="CELL"
              rx="2"
              :fill="cell ? heatColor(cell.count, selectedYearGrid.maxCount) : 'transparent'"
              :class="cell ? heatClass(cell.count, selectedYearGrid.maxCount) : ''"
              v-tooltip.top="cell ? tooltipText(cell) : undefined"
              style="cursor: default"
            />
          </template>
        </svg>

        <!-- Legend -->
        <div class="legend">
          <span class="legend-text">Less</span>
          <svg
            v-for="cls in ['heat-0', 'heat-1', 'heat-2', 'heat-3', 'heat-4']"
            :key="cls"
            width="12"
            height="12"
            class="legend-swatch"
          >
            <rect width="12" height="12" rx="2" :class="cls" />
          </svg>
          <span class="legend-text">More</span>
        </div>
      </div>
    </template>
  </Card>
</template>

<style scoped>
  .heatmap-card {
    width: 100%;
  }

  .heatmap-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
    flex-wrap: wrap;
  }

  .year-chips {
    display: flex;
    gap: 0.25rem;
    flex-wrap: wrap;
  }

  .heatmap-container {
    width: 100%;
  }

  .heatmap-svg {
    display: block;
    width: 100%;
    height: auto;
  }

  /* SVG text labels */
  .svg-label {
    font-size: 8px;
    fill: var(--p-text-muted-color);
    font-family: inherit;
  }

  /* Heat fill colors (used on <rect> elements) */
  .heat-0 {
    fill: var(--p-surface-300);
  }
  .heat-1 {
    fill: var(--p-green-800, #166534);
  }
  .heat-2 {
    fill: var(--p-green-600, #16a34a);
  }
  .heat-3 {
    fill: var(--p-green-400, #4ade80);
  }
  .heat-4 {
    fill: var(--p-green-300, #86efac);
    filter: drop-shadow(0 0 3px var(--p-green-400, #4ade80));
  }

  /* Legend */
  .legend {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 0.25rem;
    justify-content: flex-end;
  }

  .legend-swatch {
    flex-shrink: 0;
  }

  .legend-text {
    font-size: 0.6rem;
    color: var(--p-text-muted-color);
  }
</style>
