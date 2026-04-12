<script setup lang="ts">
  import { computed } from 'vue'
  import Button from 'primevue/button'
  import type { SyncStatus } from '@/composables/useSyncOperation'
  import { useSettingsNavigation } from '@/composables/useSettingsNavigation'
  import { OperationStatus, ButtonSeverity } from '@/utils/enums'

  interface RequiredSetting {
    /** Setting key to check */
    key: string
    /** Human-readable name for display */
    displayName: string
  }

  interface Props {
    /** Button label */
    label: string
    /** Required settings that must be present */
    requiredSettings?: RequiredSetting[]
    /** Current settings map to validate against */
    settings?: Record<string, string>
    /** Current sync status */
    status?: SyncStatus
    /** Button severity based on status */
    severity?: ButtonSeverity
    /** Button icon */
    icon?: string
    /** Loading state */
    loading?: boolean
    /** Additional disabled condition */
    disabled?: boolean
    /** Info message always shown */
    infoMessage?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    requiredSettings: () => [],
    settings: () => ({}),
    status: OperationStatus.IDLE,
    disabled: false,
  })

  const emit = defineEmits<{
    click: []
  }>()

  const { scrollToSetting } = useSettingsNavigation()

  // Check if all required settings are present
  const missingSettings = computed(() => {
    if (!props.requiredSettings || props.requiredSettings.length === 0) {
      return []
    }

    return props.requiredSettings.filter((req) => {
      const value = props.settings[req.key]
      return !value || value.trim() === ''
    })
  })

  const hasAllRequiredSettings = computed(() => missingSettings.value.length === 0)

  const isDisabled = computed(() => {
    return !hasAllRequiredSettings.value || props.disabled || props.loading
  })

  function handleClick() {
    if (!isDisabled.value) {
      emit('click')
    }
  }

  function handleSettingClick(settingKey: string) {
    scrollToSetting(settingKey)
  }
</script>

<template>
  <div class="sync-button-container">
    <!-- Requirement message with clickable settings -->
    <div v-if="missingSettings.length > 0" class="sync-requirement">
      <span>Requires: </span>
      <span
        v-for="(setting, index) in missingSettings"
        :key="setting.key"
        class="missing-setting"
        @click="handleSettingClick(setting.key)"
      >
        {{ setting.displayName }}
        <span v-if="index < missingSettings.length - 1">, </span>
      </span>
    </div>

    <!-- Info message -->
    <p v-if="infoMessage" class="sync-info">
      {{ infoMessage }}
    </p>

    <!-- Sync button -->
    <Button
      :label="label"
      :disabled="isDisabled"
      :severity="severity"
      :icon="icon"
      :loading="loading"
      :class="{ 'shake-animation': status === OperationStatus.ERROR }"
      @click="handleClick"
    />
  </div>
</template>

<style scoped>
  .sync-button-container {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    height: 100%;
  }

  .sync-button-container :deep(.p-button) {
    margin-top: auto;
    width: 100%;
  }

  .sync-requirement {
    font-size: 0.8rem;
    color: var(--p-text-secondary-color);
    margin: 0;
  }

  .missing-setting {
    color: var(--p-primary-color);
    cursor: pointer;
    text-decoration: underline;
    text-decoration-style: dotted;
    text-underline-offset: 2px;
    transition: all 0.2s ease;
    font-weight: 500;
  }

  .missing-setting:hover {
    color: var(--p-primary-600);
    text-decoration-style: solid;
  }

  .sync-info {
    font-size: 0.8rem;
    color: var(--p-text-secondary-color);
    margin: 0;
    padding: 0.75rem;
    border-left: 3px solid var(--p-primary-color);
    border-radius: var(--p-border-radius);
    line-height: 1.5;
  }

  /* Shake animation for error state */
  @keyframes shake {
    0%,
    100% {
      transform: translateX(0);
    }
    25% {
      transform: translateX(-8px);
    }
    75% {
      transform: translateX(8px);
    }
  }

  .shake-animation {
    animation: shake 0.4s ease-in-out;
  }
</style>
