<script setup lang="ts">
  import InputText from 'primevue/inputtext'
  import Password from 'primevue/password'
  import Checkbox from 'primevue/checkbox'
  import { computed, ref, onMounted, onUnmounted } from 'vue'
  import { useSettingsNavigation } from '@/composables/useSettingsNavigation'

  interface Props {
    /** Current value of the setting */
    modelValue: string
    /** Display label for the input */
    label: string
    /** Setting key (e.g., "jellyfin.base_url") */
    settingKey: string
    /** Input type - use "password" for API keys, "checkbox" for boolean */
    type?: 'text' | 'password' | 'checkbox'
    /** Placeholder text */
    placeholder?: string
    /** Description text shown below the input */
    description?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    type: 'text',
    placeholder: '',
    description: '',
  })

  const emit = defineEmits<{
    'update:modelValue': [value: string]
  }>()

  const { registerField, highlightedKey } = useSettingsNavigation()

  const rootElement = ref<HTMLElement | null>(null)
  const inputId = computed(() => `setting-${props.settingKey}`)

  // Check if this field is currently highlighted
  const isHighlighted = computed(() => highlightedKey.value === props.settingKey)

  // For checkbox: convert string "true"/"false" to boolean
  const checkboxValue = computed({
    get: () => props.modelValue === 'true',
    set: (value: boolean) => {
      emit('update:modelValue', value ? 'true' : 'false')
    },
  })

  function handleInput(event: Event) {
    const target = event.target as HTMLInputElement
    emit('update:modelValue', target.value)
  }

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
  <div
    ref="rootElement"
    class="setting-input"
    :class="{ 'setting-input-checkbox': type === 'checkbox', 'highlight-pulse': isHighlighted }"
  >
    <!-- Checkbox Layout -->
    <div v-if="type === 'checkbox'" class="checkbox-container">
      <Checkbox :id="inputId" v-model="checkboxValue" :binary="true" class="setting-checkbox" />
      <div class="checkbox-content">
        <label :for="inputId" class="setting-label checkbox-label">
          {{ label }}
        </label>
        <p v-if="description" class="checkbox-description">
          {{ description }}
        </p>
      </div>
    </div>

    <!-- Standard Input Layout -->
    <template v-else>
      <label :for="inputId" class="setting-label">
        {{ label }}
      </label>

      <Password
        v-if="type === 'password'"
        :id="inputId"
        :model-value="modelValue"
        :placeholder="placeholder"
        :feedback="false"
        toggle-mask
        class="setting-password"
        @input="handleInput"
      />

      <InputText
        v-else
        :id="inputId"
        :model-value="modelValue"
        :placeholder="placeholder"
        class="setting-text-input"
        @input="handleInput"
      />

      <p v-if="description" class="input-description">
        {{ description }}
      </p>
    </template>
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

  .setting-text-input,
  .setting-password {
    width: 100%;
  }

  .input-description {
    font-size: 0.8125rem;
    color: var(--p-text-secondary-color);
    margin: 0;
    line-height: 1.4;
  }

  /* Checkbox Layout */
  .setting-input-checkbox {
    margin-bottom: 1.25rem;
  }

  .checkbox-container {
    display: flex;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .setting-checkbox {
    margin-top: 0.125rem; /* Align with label baseline */
  }

  .checkbox-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .checkbox-label {
    cursor: pointer;
    margin: 0;
  }

  .checkbox-description {
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
