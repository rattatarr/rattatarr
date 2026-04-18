<script setup lang="ts">
  import Button from 'primevue/button'

  defineProps<{
    suggestions: string[]
  }>()

  const emit = defineEmits<{
    select: [suggestion: string]
  }>()
</script>

<template>
  <div class="suggestions-section">
    <span class="suggestions-label">
      <i class="pi pi-lightbulb" />
      Quick suggestions
    </span>
    <div class="suggestions-list">
      <Button
        v-for="suggestion in suggestions"
        :key="suggestion"
        :label="suggestion"
        severity="secondary"
        size="small"
        rounded
        outlined
        class="suggestion-chip"
        @click="emit('select', suggestion)"
      />
    </div>
  </div>
</template>

<style scoped>
  .suggestions-section {
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
  }

  .suggestions-label {
    font-size: 0.85rem;
    font-weight: 600;
    color: var(--p-text-secondary-color);
    display: flex;
    align-items: center;
    gap: 0.4rem;
  }

  .suggestions-list {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  .suggestion-chip {
    cursor: pointer;
    white-space: normal;
    text-align: left;
    max-width: 400px;
  }
</style>
