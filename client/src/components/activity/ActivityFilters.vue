<script setup lang="ts">
  import { computed } from 'vue'
  import DatePicker from 'primevue/datepicker'
  import Select from 'primevue/select'
  import Button from 'primevue/button'
  import { Icon } from '@/utils/enums'

  const startDate = defineModel<Date | undefined>('startDate')
  const endDate = defineModel<Date | undefined>('endDate')
  const mediaType = defineModel<'MOVIE' | 'SERIES' | undefined>('mediaType')

  const props = defineProps<{ isRefetching?: boolean; disableRefresh?: boolean }>()
  const emit = defineEmits<{ refresh: [] }>()

  const MEDIA_TYPE_OPTIONS = [
    { label: 'All types', value: undefined },
    { label: 'Movies', value: 'MOVIE' as const },
    { label: 'Series', value: 'SERIES' as const },
  ]

  const hasActiveFilters = computed(() => !!startDate.value || !!endDate.value || !!mediaType.value)

  function clearFilters() {
    startDate.value = undefined
    endDate.value = undefined
    mediaType.value = undefined
  }
</script>

<template>
  <div class="filters-panel">
    <div class="filters-row">
      <div class="filter-group">
        <label class="filter-label">From</label>
        <DatePicker
          v-model="startDate"
          show-icon
          icon-display="button"
          placeholder="Start date"
          date-format="yy-mm-dd"
          :max-date="endDate"
          fluid
        />
      </div>

      <div class="filter-group">
        <label class="filter-label">To</label>
        <DatePicker
          v-model="endDate"
          show-icon
          icon-display="button"
          placeholder="End date"
          date-format="yy-mm-dd"
          :min-date="startDate"
          fluid
        />
      </div>

      <div class="filter-group">
        <label class="filter-label">Type</label>
        <Select
          v-model="mediaType"
          :options="MEDIA_TYPE_OPTIONS"
          option-label="label"
          option-value="value"
          fluid
        />
      </div>

      <div class="filter-group filter-clear">
        <label class="filter-label">&nbsp;</label>
        <Button
          label="Clear"
          severity="secondary"
          :icon="Icon.TIMES"
          :disabled="!hasActiveFilters"
          outlined
          @click="clearFilters"
        />
      </div>

      <div class="filter-group filter-refresh">
        <label class="filter-label">&nbsp;</label>
        <Button
          label="Refresh"
          :icon="Icon.REFRESH"
          :loading="props.isRefetching"
          :disabled="props.disableRefresh"
          @click="emit('refresh')"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
  .filters-panel {
    background: var(--p-surface-card);
    border: 1px solid var(--p-surface-border);
    border-radius: var(--p-border-radius);
    padding: 0.75rem;
  }

  .filters-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr auto auto;
    gap: 0.75rem;
    align-items: end;
  }

  .filter-group {
    display: flex;
    flex-direction: column;
    gap: 0.375rem;
  }

  .filter-label {
    font-size: 0.8125rem;
    font-weight: 600;
    color: var(--p-text-color);
  }

  .filter-clear {
    justify-content: flex-end;
  }

  @media (max-width: 700px) {
    .filters-row {
      grid-template-columns: 1fr 1fr;
    }

    .filter-clear,
    .filter-refresh {
      grid-column: span 1;
    }

    .filter-clear :deep(.p-button),
    .filter-refresh :deep(.p-button) {
      width: 100%;
    }
  }

  @media (max-width: 420px) {
    .filters-row {
      grid-template-columns: 1fr;
    }

    .filter-clear,
    .filter-refresh {
      grid-column: span 1;
    }
  }
</style>
