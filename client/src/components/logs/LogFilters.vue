<script setup lang="ts">
import { ref, watch } from 'vue'
import InputText from 'primevue/inputtext'
import DatePicker from 'primevue/datepicker'
import Button from 'primevue/button'
import LogLevelChips from './LogLevelChips.vue'
import { useLogsContext } from '@/composables'

// Get logs context
const { filters, autoRefresh } = useLogsContext()

// Local state synced with context
const selectedLevel = ref<string | undefined>(filters.value.level)
const loggerName = ref<string>(filters.value.logger ?? '')
const startDate = ref<Date | undefined>(
  filters.value.startDate ? new Date(filters.value.startDate) : undefined,
)
const endDate = ref<Date | undefined>(
  filters.value.endDate ? new Date(filters.value.endDate) : undefined,
)

// Watch for changes and update context
watch([selectedLevel, loggerName, startDate, endDate], () => {
  filters.value = {
    level: selectedLevel.value,
    logger: loggerName.value || undefined,
    startDate: startDate.value?.toISOString(),
    endDate: endDate.value?.toISOString(),
  }
})

// Auto-refresh toggle
const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
}
</script>

<template>
  <div class="log-filters">
    <div class="filters-row">
      <!-- Level Filter -->
      <div class="filter-group">
        <label class="filter-label">Levels</label>
        <LogLevelChips v-model="selectedLevel" />
      </div>

      <!-- Logger Filter -->
      <div class="filter-group">
        <label for="logger-filter" class="filter-label">Logger Name</label>
        <InputText
          id="logger-filter"
          v-model="loggerName"
          placeholder="e.g. com.rattatarr"
          class="filter-input"
        />
      </div>

      <!-- Date Range -->
      <div class="filter-group">
        <label for="start-date" class="filter-label">From</label>
        <DatePicker
          id="start-date"
          v-model="startDate"
          show-time
          hour-format="24"
          show-icon
          icon-display="button"
          placeholder="Start date/time"
          class="filter-input"
          date-format="yy-mm-dd"
          fluid
        />
      </div>

      <div class="filter-group">
        <label for="end-date" class="filter-label">To</label>
        <DatePicker
          id="end-date"
          v-model="endDate"
          show-time
          hour-format="24"
          show-icon
          icon-display="button"
          placeholder="End date/time"
          class="filter-input"
          date-format="yy-mm-dd"
          fluid
        />
      </div>

      <!-- Auto-Refresh Toggle (Far Right) -->
      <div class="filter-group filter-auto-refresh">
        <label class="filter-label">&nbsp;</label>
        <Button
          :label="autoRefresh ? 'Auto-refresh: ON' : 'Auto-refresh: OFF'"
          :severity="autoRefresh ? 'success' : 'secondary'"
          :icon="autoRefresh ? 'pi pi-sync pi-spin' : 'pi pi-sync'"
          size="small"
          outlined
          @click="toggleAutoRefresh"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.log-filters {
  padding: 0.75rem;
  background: var(--p-surface-900);
  border: 1px solid var(--p-surface-border);
  border-radius: var(--p-border-radius);
}

.filters-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 0.25rem;
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

.filter-input {
  width: 100%;
}

/* Ensure DatePicker and InputText take full width */
.filter-group :deep(.p-inputtext),
.filter-group :deep(.p-datepicker) {
  width: 100%;
}

.filter-actions {
  justify-content: flex-end;
}

.filter-auto-refresh {
  justify-content: flex-end;
  margin-left: auto;
}

.filter-auto-refresh :deep(.p-button) {
  width: auto;
  min-width: 2.5rem;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .filters-row {
    grid-template-columns: 1fr;
    gap: 0.25rem;
  }

  .filter-input {
    width: 100%;
  }

  .filter-actions {
    justify-content: stretch;
  }

  .filter-actions :deep(.p-button) {
    width: 100%;
  }

  .filter-auto-refresh {
    margin-left: 0;
  }

  .filter-auto-refresh :deep(.p-button) {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .log-filters {
    padding: 0.5rem;
  }

  .filter-label {
    font-size: 0.75rem;
  }
}
</style>
