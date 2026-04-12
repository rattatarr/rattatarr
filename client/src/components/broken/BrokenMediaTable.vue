<script setup lang="ts">
  import Button from 'primevue/button'
  import Column from 'primevue/column'
  import DataTable from 'primevue/datatable'
  import Tag from 'primevue/tag'
  import type { BrokenMediaItem } from '@/types'
  import { Icon } from '@/utils/enums'

  interface Props {
    items: BrokenMediaItem[]
    loading?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    loading: false,
  })

  const emit = defineEmits<{
    resolve: [item: BrokenMediaItem]
  }>()
</script>

<template>
  <DataTable :value="props.items" :loading="props.loading" striped-rows class="broken-table">
    <Column field="title" header="Title" />
    <Column field="productionYear" header="Year" style="width: 6rem" />
    <Column field="missingFields" header="Missing Fields">
      <template #body="{ data }">
        <div class="missing-fields">
          <Tag
            v-for="field in (data.missingFields ?? '').split(',').filter(Boolean)"
            :key="field"
            :value="field.trim()"
            severity="secondary"
            class="field-tag"
          />
        </div>
      </template>
    </Column>
    <Column field="resolved" header="Status" style="width: 7rem">
      <template #body="{ data }">
        <i :class="data.resolved ? Icon.CHECK_CIRCLE : Icon.TIMES_CIRCLE" class="status-icon" />
      </template>
    </Column>
    <Column header="" style="width: 7rem">
      <template #body="{ data }">
        <Button
          label="Resolve"
          :icon="Icon.CHECK"
          size="small"
          outlined
          :disabled="data.resolved"
          @click="emit('resolve', data)"
        />
      </template>
    </Column>
  </DataTable>
</template>

<style scoped>
  .missing-fields {
    display: flex;
    flex-wrap: wrap;
    gap: 0.25rem;
  }

  .field-tag {
    font-size: 0.75rem;
  }

  .status-icon {
    color: var(--p-text-muted-color);
  }
</style>
