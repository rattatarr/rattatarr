<script setup lang="ts">
  import Column from 'primevue/column'
  import DataTable from 'primevue/datatable'
  import JobStatusTag from './JobStatusTag.vue'
  import type { BackgroundJob, JobType } from '@/types'

  defineProps<{
    jobs: BackgroundJob[]
    totalRecords: number
    rows: number
    loading?: boolean
  }>()

  const emit = defineEmits<{
    page: [{ page: number; rows: number }]
  }>()

  function formatType(type?: JobType): string {
    switch (type) {
      case 'JELLYFIN_SYNC':
        return 'Jellyfin Sync'
      case 'CSV_IMPORT':
        return 'CSV Import'
      default:
        return type ?? '—'
    }
  }

  function formatDate(iso?: string): string {
    if (!iso) return '—'
    return new Date(iso).toLocaleString()
  }
</script>

<template>
  <DataTable
    :value="jobs"
    :loading="loading"
    lazy
    paginator
    :rows="rows"
    :total-records="totalRecords"
    :rows-per-page-options="[10, 20, 50]"
    class="jobs-table"
    @page="emit('page', { page: $event.page, rows: $event.rows })"
  >
    <template #empty>
      <div class="jobs-table__empty">
        <i class="pi pi-inbox" />
        <p>No jobs found</p>
      </div>
    </template>

    <Column field="type" header="Type" style="width: 14rem">
      <template #body="{ data: job }: { data: BackgroundJob }">
        {{ formatType(job.type) }}
      </template>
    </Column>

    <Column field="status" header="Status" style="width: 10rem">
      <template #body="{ data: job }: { data: BackgroundJob }">
        <JobStatusTag :status="job.status" />
      </template>
    </Column>

    <Column field="message" header="Message">
      <template #body="{ data: job }: { data: BackgroundJob }">
        <span class="jobs-table__message">{{ job.message ?? '—' }}</span>
      </template>
    </Column>

    <Column field="startedAt" header="Started" style="width: 14rem">
      <template #body="{ data: job }: { data: BackgroundJob }">
        {{ formatDate(job.startedAt) }}
      </template>
    </Column>

    <Column field="finishedAt" header="Finished" style="width: 14rem">
      <template #body="{ data: job }: { data: BackgroundJob }">
        {{ formatDate(job.finishedAt) }}
      </template>
    </Column>

    <Column field="createdAt" header="Created" style="width: 14rem">
      <template #body="{ data: job }: { data: BackgroundJob }">
        {{ formatDate(job.createdAt) }}
      </template>
    </Column>
  </DataTable>
</template>

<style scoped>
  .jobs-table {
    width: 100%;
  }

  .jobs-table__empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
    padding: 3rem;
    color: var(--p-text-secondary-color);
  }

  .jobs-table__message {
    font-size: 0.875rem;
    color: var(--p-text-secondary-color);
    white-space: pre-wrap;
    word-break: break-word;
  }
</style>
