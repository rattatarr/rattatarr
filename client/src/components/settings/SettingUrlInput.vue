<script setup lang="ts">
  import { computed, ref, onMounted, onUnmounted } from 'vue'
  import InputText from 'primevue/inputtext'
  import InputGroup from 'primevue/inputgroup'
  import InputGroupAddon from 'primevue/inputgroupaddon'
  import Select from 'primevue/select'
  import { useSettingsNavigation } from '@/composables/useSettingsNavigation'

  interface Props {
    /** Current value of the setting (full URL with prefix) */
    modelValue: string
    /** Display label for the input */
    label: string
    /** Setting key (e.g., "jellyfin.base_url") */
    settingKey: string
    /** Placeholder text for the URL part (without prefix) */
    placeholder?: string
    /** Description text shown below the input */
    description?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    placeholder: '',
    description: '',
  })

  const emit = defineEmits<{
    'update:modelValue': [value: string]
  }>()

  const { registerField, highlightedKey } = useSettingsNavigation()

  const rootElement = ref<HTMLElement | null>(null)
  const inputId = computed(() => `setting-${props.settingKey}`)

  const prefixOptions = [
    { label: 'http://', value: 'http://' },
    { label: 'https://', value: 'https://' },
  ]

  // Parse the modelValue to extract prefix and URL part
  const selectedPrefix = computed({
    get: () => {
      const value = props.modelValue ?? ''
      if (value.startsWith('https://')) return 'https://'
      return 'http://'
    },
    set: (prefix: string) => {
      emit('update:modelValue', prefix + urlPart.value)
    },
  })

  const urlPart = computed({
    get: () => {
      const value = props.modelValue ?? ''
      if (value.startsWith('https://')) return value.slice(8)
      if (value.startsWith('http://')) return value.slice(7)
      return value
    },
    set: (url: string) => {
      emit('update:modelValue', selectedPrefix.value + url)
    },
  })

  // Check if this field is currently highlighted
  const isHighlighted = computed(() => highlightedKey.value === props.settingKey)

  // Register this field on mount
  onMounted(() => {
    if (rootElement.value) {
      registerField(props.settingKey, rootElement.value)
    }
  })

  // Unregister on unmount
  onUnmounted(() => {
    registerField(props.settingKey, null)
  })
</script>

<template>
  <div ref="rootElement" class="setting-input" :class="{ 'highlight-pulse': isHighlighted }">
    <label :for="inputId" class="setting-label">
      {{ label }}
    </label>

    <InputGroup>
      <InputGroupAddon class="prefix-addon">
        <Select
          v-model="selectedPrefix"
          :options="prefixOptions"
          option-label="label"
          option-value="value"
          class="prefix-select"
        />
      </InputGroupAddon>
      <InputText :id="inputId" v-model="urlPart" :placeholder="placeholder" class="url-input" />
    </InputGroup>

    <p v-if="description" class="input-description">
      {{ description }}
    </p>
  </div>
</template>

<style scoped>
  .setting-input {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-bottom: 1rem;
  }

  .setting-label {
    font-weight: 500;
    color: var(--p-text-color);
    font-size: 0.875rem;
  }

  .prefix-addon {
    padding: 0;
    overflow: hidden;
  }

  .prefix-select {
    border: none;
    background: transparent;
    min-width: 7rem;
  }

  .url-input {
    flex: 1;
  }

  .input-description {
    font-size: 0.8125rem;
    color: var(--p-text-secondary-color);
    margin: 0;
    line-height: 1.4;
  }

  /* Last setting input in a card shouldn't have bottom margin */
  .setting-input:last-child {
    margin-bottom: 0;
  }

  /* Highlight animation when scrolled to */
  @keyframes pulse-highlight {
    0%,
    100% {
      box-shadow: 0 0 0 0 rgba(255, 255, 255, 0);
    }
    50% {
      box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.3);
    }
  }

  .highlight-pulse {
    padding: 0.75rem;
    margin: -0.75rem -0.75rem calc(1rem - 0.75rem);
    border-radius: var(--p-border-radius);
    background: rgba(255, 255, 255, 0.05);
    animation: pulse-highlight 1s ease-out 3;
    transition: all 0.3s ease;
  }

  .highlight-pulse:last-child {
    margin-bottom: -0.75rem;
  }
</style>
