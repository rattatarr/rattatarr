<script setup lang="ts">
  import Tag from 'primevue/tag'
  import type { JobStatus } from '@/types'

  const props = defineProps<{ status?: JobStatus }>()

  type Severity = 'success' | 'info' | 'warn' | 'danger' | 'secondary'

  function severity(status?: JobStatus): Severity {
    switch (status) {
      case 'COMPLETED':
        return 'success'
      case 'RUNNING':
        return 'info'
      case 'PENDING':
        return 'warn'
      case 'FAILED':
        return 'danger'
      default:
        return 'secondary'
    }
  }
</script>

<template>
  <Tag :severity="severity(props.status)" :value="props.status ?? '—'" />
</template>
