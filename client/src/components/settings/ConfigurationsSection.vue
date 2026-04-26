<script setup lang="ts">
  import { SettingsCard, SettingInput } from '@/components/settings'
  import { useSettingsForm } from '@/composables/useSettingsForm'

  const { updateSetting, getSetting } = useSettingsForm()

  const configKeys = {
    autoRefresh: 'sync.auto_refresh_on_read',
    radarrEnabled: 'sync.radarr_enabled',
    sonarrEnabled: 'sync.sonarr_enabled',
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
        <SettingInput
          :model-value="getSetting(configKeys.radarrEnabled)"
          label="Enable Radarr Sync"
          :setting-key="configKeys.radarrEnabled"
          type="checkbox"
          description="Periodically fetch IMDb and Rotten Tomatoes ratings from Radarr and import newly added movies into your library"
          @update:model-value="(v) => updateSetting(configKeys.radarrEnabled, v)"
        />
        <SettingInput
          :model-value="getSetting(configKeys.sonarrEnabled)"
          label="Enable Sonarr Sync"
          :setting-key="configKeys.sonarrEnabled"
          type="checkbox"
          description="Periodically import newly added series from Sonarr into your library"
          @update:model-value="(v) => updateSetting(configKeys.sonarrEnabled, v)"
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
