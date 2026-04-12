<script setup lang="ts">
  import { ref } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import { Icon, OperationStatus, ButtonSeverity } from '@/utils'

  export type ConnectionStatus = OperationStatus

  export interface ConnectionTestConfig {
    /** Function to call for testing connection */
    testFn: () => Promise<{ status?: string; message?: string }>
    /** Optional success message for toast */
    successMessage?: string
    /** Optional label override for the button */
    buttonLabel?: string
    /** Keys to save before testing connection */
    preSaveKeys?: string[]
    /** Function to save specific settings before testing */
    saveSpecificSettings?: (keys: string[]) => Promise<boolean>
  }

  interface Props {
    /** Card title */
    title: string
    /** Optional description shown below title */
    description?: string
    /** Optional connection test configuration */
    connectionTest?: ConnectionTestConfig
  }

  const props = defineProps<Props>()

  // Connection test state
  const connectionStatus = ref<ConnectionStatus>(OperationStatus.IDLE)
  const isTesting = ref(false)

  async function handleConnectionTest() {
    if (!props.connectionTest) return

    connectionStatus.value = OperationStatus.TESTING
    isTesting.value = true

    try {
      // Pre-save settings if configured
      if (props.connectionTest.preSaveKeys && props.connectionTest.saveSpecificSettings) {
        const saveSuccess = await props.connectionTest.saveSpecificSettings(
          props.connectionTest.preSaveKeys,
        )
        if (!saveSuccess) {
          connectionStatus.value = OperationStatus.ERROR
          return
        }
      }

      const response = await props.connectionTest.testFn()

      // Check if response indicates success
      const isSuccess =
        response.status?.includes('200') ||
        (!response.message?.toLowerCase().includes('failed') &&
          !response.message?.toLowerCase().includes('error'))

      connectionStatus.value = isSuccess ? OperationStatus.SUCCESS : OperationStatus.ERROR
    } catch {
      connectionStatus.value = OperationStatus.ERROR
    } finally {
      isTesting.value = false

      // Reset status after 3 seconds
      setTimeout(() => {
        connectionStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  function getButtonLabel(): string {
    if (connectionStatus.value === OperationStatus.SUCCESS) return 'Connected'
    if (props.connectionTest?.buttonLabel) return props.connectionTest.buttonLabel
    return 'Check Connection'
  }
</script>

<template>
  <Card class="settings-card">
    <template #title>
      <h2 class="card-title">{{ title }}</h2>
    </template>

    <template #content>
      <div class="card-content-wrapper">
        <p v-if="description" class="card-description">
          {{ description }}
        </p>

        <!-- Slot for SettingInput components -->
        <div class="settings-content">
          <slot />
        </div>

        <!-- Connection test button -->
        <Button
          v-if="connectionTest"
          :label="getButtonLabel()"
          :icon="connectionStatus === OperationStatus.SUCCESS ? Icon.CHECK : undefined"
          :severity="
            connectionStatus === OperationStatus.ERROR
              ? ButtonSeverity.DANGER
              : connectionStatus === OperationStatus.SUCCESS
                ? ButtonSeverity.SUCCESS
                : ButtonSeverity.SECONDARY
          "
          :loading="isTesting"
          :class="{ 'connection-test-shake': connectionStatus === OperationStatus.ERROR }"
          outlined
          class="connection-test-btn"
          @click="handleConnectionTest"
        />
      </div>
    </template>
  </Card>
</template>

<style scoped>
  .settings-card {
    height: 100%;
  }

  /* Make PrimeVue Card's content area fill the full card height */
  :deep(.p-card-body) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  :deep(.p-card-content) {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .card-content-wrapper {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .card-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--p-text-color);
    margin: 0;
  }

  .card-description {
    color: var(--p-text-secondary-color);
    margin: 0 0 1.25rem 0;
    font-size: 0.875rem;
    line-height: 1.5;
  }

  .settings-content {
    display: flex;
    flex-direction: column;
    flex: 1;
  }

  /* Connection test button */
  .connection-test-btn {
    margin-top: 1rem;
    width: 100%;
  }

  /* Shake animation for error state */
  .connection-test-shake {
    animation: shake 0.5s;
  }

  @keyframes shake {
    0%,
    100% {
      transform: translateX(0);
    }
    10%,
    30%,
    50%,
    70%,
    90% {
      transform: translateX(-8px);
    }
    20%,
    40%,
    60%,
    80% {
      transform: translateX(8px);
    }
  }
</style>
