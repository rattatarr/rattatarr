<script setup lang="ts">
import { ref, watch } from 'vue'
import Chip from 'primevue/chip'

interface Props {
  modelValue?: string
}

interface Emits {
  (e: 'update:modelValue', value: string | undefined): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const levels = [
  { value: 'ERROR', label: 'ERROR', color: 'red' },
  { value: 'WARN', label: 'WARN', color: 'orange' },
  { value: 'INFO', label: 'INFO', color: 'blue' },
  { value: 'DEBUG', label: 'DEBUG', color: 'gray' },
] as const

const selectedLevel = ref<string | undefined>(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    selectedLevel.value = newValue
  },
)

const toggleLevel = (level: string) => {
  if (selectedLevel.value === level) {
    selectedLevel.value = undefined
  } else {
    selectedLevel.value = level
  }
  emit('update:modelValue', selectedLevel.value)
}

const isSelected = (level: string) => selectedLevel.value === level
</script>

<template>
  <div class="log-level-chips">
    <Chip
      v-for="level in levels"
      :key="level.value"
      :label="level.label"
      :class="[
        'level-chip',
        `level-chip-${level.color}`,
        { 'level-chip-selected': isSelected(level.value) },
      ]"
      @click="toggleLevel(level.value)"
    />
  </div>
</template>

<style scoped>
.log-level-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.level-chip {
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
  font-size: 0.8125rem;
  opacity: 0.6;
  border: 2px solid transparent;
}

.level-chip:hover {
  opacity: 1;
  transform: translateY(-1px);
}

.level-chip-selected {
  opacity: 1;
  border-color: currentColor;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* Color variants */
.level-chip-red {
  background: var(--p-red-50);
  color: var(--p-red-700);
}

.level-chip-red.level-chip-selected {
  background: var(--p-red-100);
}

.level-chip-orange {
  background: var(--p-orange-50);
  color: var(--p-orange-700);
}

.level-chip-orange.level-chip-selected {
  background: var(--p-orange-100);
}

.level-chip-blue {
  background: var(--p-blue-50);
  color: var(--p-blue-700);
}

.level-chip-blue.level-chip-selected {
  background: var(--p-blue-100);
}

.level-chip-gray {
  background: var(--p-gray-50);
  color: var(--p-gray-700);
}

.level-chip-gray.level-chip-selected {
  background: var(--p-gray-100);
}
</style>
