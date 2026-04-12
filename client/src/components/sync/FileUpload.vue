<script setup lang="ts">
  import { computed, watch } from 'vue'
  import { useFileDialog } from '@vueuse/core'
  import Button from 'primevue/button'
  import { ButtonSeverity, Icon } from '@/utils/enums'

  interface Props {
    /** Currently selected file */
    modelValue: File | null
    /** Accepted file extensions (e.g., '.csv,.txt') */
    accept?: string
    /** Label to show when no file is selected */
    placeholder?: string
    /** Whether the input is disabled */
    disabled?: boolean
    /** Icon to display */
    icon?: string
    /** Allow multiple files */
    multiple?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    accept: '*',
    placeholder: 'Choose File',
    disabled: false,
    icon: Icon.FILE,
    multiple: false,
  })

  const emit = defineEmits<{
    'update:modelValue': [file: File | null]
    change: [file: File | null]
  }>()

  const buttonLabel = computed(() => {
    return props.modelValue ? props.modelValue.name : props.placeholder
  })

  // Use VueUse's useFileDialog
  const { files, open, reset } = useFileDialog({
    accept: props.accept,
    multiple: props.multiple,
  })

  // Watch for file changes and emit
  watch(files, (newFiles) => {
    const file = newFiles?.[0] || null
    emit('update:modelValue', file)
    emit('change', file)
  })

  function openFilePicker() {
    if (!props.disabled) {
      open()
    }
  }

  function clear() {
    reset()
    emit('update:modelValue', null)
    emit('change', null)
  }

  // Expose methods for parent components
  defineExpose({
    open: openFilePicker,
    clear,
  })
</script>

<template>
  <div class="file-upload-container">
    <Button
      :label="buttonLabel"
      :icon="icon"
      :severity="ButtonSeverity.SECONDARY"
      outlined
      :disabled="disabled"
      class="file-select-button"
      @click="openFilePicker"
    />
  </div>
</template>

<style scoped>
  .file-upload-container {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .file-select-button {
    width: 100%;
    justify-content: flex-start;
  }

  .file-select-button :deep(.p-button-label) {
    color: var(--p-text-color);
    font-weight: 500;
  }

  .file-select-button :deep(.p-button-icon) {
    color: var(--p-surface-0);
  }
</style>
