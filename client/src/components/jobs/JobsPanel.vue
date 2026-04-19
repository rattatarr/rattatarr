<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import Button from 'primevue/button'
  import ProgressSpinner from 'primevue/progressspinner'
  import JobsFilters from './JobsFilters.vue'
  import JobsTable from './JobsTable.vue'
  import { useJobs } from '@/queries/useJobs'
  import { useQueryClient } from '@tanstack/vue-query'
  import { jobKeys } from '@/queries/queryKeys'
  import { Icon } from '@/utils/enums'
  import type { JobsFilters as JobsFiltersDTO, Pageable } from '@/types'
  import type { JobsFilterState } from './JobsFilters.vue'

  const queryClient = useQueryClient()

  const pageable = ref<Pageable>({ page: 0, size: 20 })
  const filterState = ref<JobsFilterState>({})

  const apiFilters = computed<JobsFiltersDTO>(() => ({
    type: filterState.value.type,
    status: filterState.value.status,
  }))

  // Reset to page 0 when filters change
  watch(filterState, () => {
    pageable.value = { ...pageable.value, page: 0 }
  })

  const { data, isLoading, error } = useJobs(pageable, apiFilters)

  const jobs = computed(() => data.value?.jobs ?? [])
  const totalRecords = computed(() => data.value?.pagination?.totalElements ?? 0)

  function refresh() {
    void queryClient.invalidateQueries({ queryKey: jobKeys.all })
  }

  function onPage(event: { page: number; rows: number }) {
    pageable.value = { page: event.page, size: event.rows }
  }
</script>

<template>
  <div class="jobs-panel">
    <div class="jobs-panel__header">
      <h2 class="jobs-panel__title">Background Jobs</h2>
      <Button :icon="Icon.REFRESH" severity="secondary" text @click="refresh" />
    </div>

    <JobsFilters v-model="filterState" />

    <div v-if="isLoading" class="jobs-panel__state">
      <ProgressSpinner />
      <p>Loading jobs…</p>
    </div>

    <div v-else-if="error" class="jobs-panel__state jobs-panel__state--error">
      <i :class="Icon.EXCLAMATION_TRIANGLE" />
      <p>Failed to load jobs: {{ error.message }}</p>
    </div>

    <JobsTable
      v-else
      :jobs="jobs"
      :total-records="totalRecords"
      :rows="pageable.size ?? 20"
      @page="onPage"
    />
  </div>
</template>

<style scoped>
  .jobs-panel {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .jobs-panel__header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .jobs-panel__title {
    margin: 0;
    flex: 1;
  }

  .jobs-panel__state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
    padding: 3rem;
    color: var(--p-text-secondary-color);
  }

  .jobs-panel__state--error {
    color: var(--p-red-500);
  }
</style>
