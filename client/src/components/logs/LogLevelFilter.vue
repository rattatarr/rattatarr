<script setup lang="ts">
import { ref, watch } from 'vue'
import LogLevelChips from './LogLevelChips.vue'

interface Props {
  modelValue?: string
}

interface Emits {
  (e: 'update:modelValue', value: string | undefined): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const selectedLevel = ref<string | undefined>(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    selectedLevel.value = newValue
  },
)

watch(selectedLevel, (newValue) => {
  emit('update:modelValue', newValue)
})
</script>

<template>
  <div class="log-level-filter">
    <span class="filter-label">Filter:</span>
    <LogLevelChips v-model="selectedLevel" />
  </div>
</template>

<style scoped>
.log-level-filter {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--p-text-color);
  flex-shrink: 0;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .log-level-filter {
    width: 100%;
    gap: 0.5rem;
  }

  .filter-label {
    font-size: 0.8125rem;
  }
}

@media (max-width: 480px) {
  .log-level-filter {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
