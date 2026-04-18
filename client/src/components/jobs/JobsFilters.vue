<script setup lang="ts">
  import Select from 'primevue/select'
  import Button from 'primevue/button'
  import type { JobStatus, JobType } from '@/types'

  export interface JobsFilterState {
    type?: JobType
    status?: JobStatus
  }

  const model = defineModel<JobsFilterState>({ required: true })

  const typeOptions: Array<{ label: string; value: JobType | undefined }> = [
    { label: 'All types', value: undefined },
    { label: 'Jellyfin Sync', value: 'JELLYFIN_SYNC' },
    { label: 'CSV Import', value: 'CSV_IMPORT' },
  ]

  const statusOptions: Array<{ label: string; value: JobStatus | undefined }> = [
    { label: 'All statuses', value: undefined },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Running', value: 'RUNNING' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Failed', value: 'FAILED' },
  ]

  function setType(v: JobType | undefined) {
    model.value = { ...model.value, type: v }
  }

  function setStatus(v: JobStatus | undefined) {
    model.value = { ...model.value, status: v }
  }

  function reset() {
    model.value = {}
  }
</script>

<template>
  <div class="jobs-filters">
    <Select
      :model-value="model.type"
      :options="typeOptions"
      option-label="label"
      option-value="value"
      placeholder="All types"
      class="jobs-filters__select"
      @update:model-value="setType"
    />
    <Select
      :model-value="model.status"
      :options="statusOptions"
      option-label="label"
      option-value="value"
      placeholder="All statuses"
      class="jobs-filters__select"
      @update:model-value="setStatus"
    />
    <Button label="Reset" severity="secondary" text @click="reset" />
  </div>
</template>

<style scoped>
  .jobs-filters {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    flex-wrap: wrap;
  }

  .jobs-filters__select {
    min-width: 12rem;
  }
</style>
