<script setup lang="ts">
  import Button from 'primevue/button'
  import Divider from 'primevue/divider'
  import {
    ExternalSyncSection,
    ConfigurationsSection,
    IntegrationsSection,
  } from '@/components/settings'
  import { provideSettingsForm } from '@/composables/useSettingsForm'
  import { provideSettingsNavigation } from '@/composables/useSettingsNavigation'
  import { useNavigation } from '@/composables'
  import { ButtonSeverity, Icon } from '@/utils/enums'

  // Provide settings form context to all child components
  const { hasChanges, isLoading, isSaving, saveSettings, resetSettings } = provideSettingsForm()

  // Provide settings navigation context for clickable missing settings
  provideSettingsNavigation()

  const { goToHowTo } = useNavigation()
</script>

<template>
  <div class="settings-view">
    <div class="settings-header">
      <div class="page-title-container">
        <h1 class="page-title">Settings</h1>
        <i v-if="isLoading" :class="[Icon.SPINNER, 'pi-spin', 'loading-spinner']"></i>
      </div>
      <div class="settings-actions">
        <Button
          label="Reset"
          :severity="ButtonSeverity.SECONDARY"
          outlined
          :disabled="!hasChanges"
          @click="resetSettings"
        />
        <Button
          label="Save Changes"
          :disabled="!hasChanges"
          :loading="isSaving"
          @click="saveSettings"
        />
      </div>
    </div>

    <!-- Help Banner -->
    <div class="help-banner">
      <div class="help-content">
        <i :class="[Icon.INFO_CIRCLE, 'help-icon']"></i>
        <div class="help-text">
          <strong>Need help getting your API keys?</strong>
          <span
            >Check out our step-by-step guides for TMDb, Jellyfin, Sonarr, Radarr, and IMDb.</span
          >
        </div>
      </div>
      <Button
        label="How To Get API Keys"
        :icon="Icon.QUESTION_CIRCLE"
        size="small"
        outlined
        @click="goToHowTo"
      />
    </div>

    <!-- Settings Content -->
    <div class="settings-container">
      <ExternalSyncSection />

      <Divider />

      <ConfigurationsSection />

      <Divider />

      <IntegrationsSection />

      <!-- Save Actions (Bottom) -->
      <div v-if="hasChanges" class="settings-footer">
        <div class="footer-message">
          <i :class="Icon.INFO_CIRCLE"></i>
          <span>You have unsaved changes</span>
        </div>
        <div class="footer-actions">
          <Button
            label="Reset"
            :severity="ButtonSeverity.SECONDARY"
            outlined
            @click="resetSettings"
          />
          <Button label="Save Changes" :loading="isSaving" @click="saveSettings" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .settings-view {
    padding-bottom: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .settings-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    flex-wrap: wrap;
    gap: 1rem;
  }

  .page-title-container {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .page-title {
    margin: 0;
    color: var(--p-text-color);
    font-size: 2rem;
    font-weight: 600;
  }

  .loading-spinner {
    font-size: 1.5rem;
    color: var(--p-primary-color);
  }

  .settings-actions {
    display: flex;
    gap: 0.75rem;
  }

  /* Help Banner */
  .help-banner {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.25rem 1.5rem;
    background: var(--p-primary-800);
    border: 1px solid var(--p-primary-200);
    border-radius: var(--p-border-radius);
    margin-bottom: 2rem;
    gap: 1rem;
  }

  .help-content {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex: 1;
  }

  .help-icon {
    font-size: 1.5rem;
    color: var(--p-primary-color);
    flex-shrink: 0;
  }

  .help-text {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  .help-text strong {
    color: var(--p-text-color);
    font-size: 0.9375rem;
  }

  .help-text span {
    color: var(--p-text-secondary-color);
    font-size: 0.875rem;
  }

  .settings-container {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }

  .settings-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem;
    background: var(--p-surface-700);
    border-radius: var(--p-border-radius);
    margin-top: 1rem;
    flex-wrap: wrap;
    gap: 1rem;
  }

  .footer-message {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--p-primary-color);
    font-weight: 500;
  }

  .footer-actions {
    display: flex;
    gap: 0.75rem;
  }

  /* Responsive adjustments */
  @media (max-width: 768px) {
    .settings-header {
      flex-direction: column;
      align-items: flex-start;
    }

    .settings-actions {
      width: 100%;
    }

    .settings-actions :deep(button) {
      flex: 1;
    }

    .help-banner {
      flex-direction: column;
      align-items: flex-start;
    }

    .help-banner :deep(button) {
      width: 100%;
    }

    .settings-footer {
      flex-direction: column;
      align-items: stretch;
    }

    .footer-actions {
      width: 100%;
    }

    .footer-actions :deep(button) {
      flex: 1;
    }
  }
</style>
