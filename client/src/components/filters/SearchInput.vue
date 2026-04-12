<script setup lang="ts">
  import { ref, watch } from 'vue'
  import InputText from 'primevue/inputtext'
  import IconField from 'primevue/iconfield'
  import InputIcon from 'primevue/inputicon'
  import { Icon } from '@/utils/enums'
  import { useDebounceFn } from '@vueuse/core'

  interface Props {
    modelValue: string
    placeholder?: string
    debounce?: number
  }

  interface Emits {
    (e: 'update:modelValue', value: string): void
  }

  const props = withDefaults(defineProps<Props>(), {
    placeholder: 'Search...',
    debounce: 500,
  })

  const emit = defineEmits<Emits>()

  const localValue = ref(props.modelValue)

  // Debounced emit
  const debouncedEmit = useDebounceFn((value: string) => {
    emit('update:modelValue', value)
  }, props.debounce)

  function onInput() {
    debouncedEmit(localValue.value)
  }

  function clear() {
    localValue.value = ''
    emit('update:modelValue', '')
  }

  // Sync with external changes
  watch(
    () => props.modelValue,
    (newValue) => {
      if (newValue !== localValue.value) {
        localValue.value = newValue
      }
    },
  )
</script>

<template>
  <div class="search-input">
    <IconField>
      <InputIcon :class="Icon.SEARCH" />
      <InputText
        v-model="localValue"
        :placeholder="placeholder"
        class="search-field"
        @input="onInput"
      />
      <InputIcon v-if="localValue" :class="Icon.TIMES" class="clear-icon" @click="clear" />
    </IconField>
  </div>
</template>

<style scoped>
  .search-input {
    width: 100%;
    max-width: 100%;
    min-width: 0;
  }

  .search-field {
    width: 100%;
    max-width: 100%;
  }

  .clear-icon {
    cursor: pointer;
    opacity: 0.6;
    transition: opacity 0.2s;
  }

  .clear-icon:hover {
    opacity: 1;
  }
</style>
