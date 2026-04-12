<script setup lang="ts">
  import { SettingsCard, SettingInput } from '@/components/settings'
  import { useSettingsForm } from '@/composables/useSettingsForm'

  const { updateSetting, getSetting } = useSettingsForm()

  const configKeys = {
    autoRefresh: 'sync.auto_refresh_on_read',
  }
</script>

<template>
  <section class="settings-section">
    <h2 class="section-title">Configurations</h2>
    <p class="section-description">Application-level configuration options</p>

    <div class="settings-grid">
      <SettingsCard title="Sync Settings" description="Configure synchronization behavior">
        <SettingInput
          :model-value="getSetting(configKeys.autoRefresh)"
          label="Auto Refresh on Read"
          :setting-key="configKeys.autoRefresh"
          type="checkbox"
          description="Automatically refresh media data when reading from external sources"
          @update:model-value="(v) => updateSetting(configKeys.autoRefresh, v)"
        />
      </SettingsCard>
    </div>
  </section>
</template>

<style scoped>
  .settings-section {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .section-title {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--p-text-color);
    margin: 0;
  }

  .section-description {
    color: var(--p-text-secondary-color);
    margin: 0;
    font-size: 0.875rem;
  }

  .settings-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 1.5rem;
    margin-top: 1rem;
  }

  @media (max-width: 768px) {
    .settings-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
