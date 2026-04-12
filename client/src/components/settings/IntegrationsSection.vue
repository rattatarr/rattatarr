<script setup lang="ts">
  import { SettingsCard, SettingInput, SettingUrlInput } from '@/components/settings'
  import type { ConnectionTestConfig } from '@/components/settings/SettingsCard.vue'
  import { useToast } from '@/composables/useToast'
  import { useSettingsForm } from '@/composables/useSettingsForm'
  import { useJellyfinTest, useTMDbTest } from '@/queries'

  const toast = useToast()
  const { updateSetting, getSetting, saveSpecificSettings } = useSettingsForm()

  const jellyfinTest = useJellyfinTest()
  const tmdbTest = useTMDbTest()

  const integrationKeys = {
    jellyfinUrl: 'jellyfin.base_url',
    jellyfinKey: 'jellyfin.api_key',
    tmdbKey: 'tmdb.api_key',
    sonarrUrl: 'sonarr.base_url',
    sonarrKey: 'sonarr.api_key',
    radarrUrl: 'radarr.base_url',
    radarrKey: 'radarr.api_key',
  }

  // Connection test configurations
  const jellyfinConnectionTest: ConnectionTestConfig = {
    preSaveKeys: [integrationKeys.jellyfinUrl, integrationKeys.jellyfinKey],
    saveSpecificSettings,
    testFn: async () => {
      await jellyfinTest.refetch()

      const response = jellyfinTest.data?.value
      const error = jellyfinTest.error?.value

      if (error) {
        toast.error('Failed to connect', error.message)
        return { status: '500', message: error.message }
      }

      if (
        response?.status?.includes('200') ||
        (!response?.message?.toLowerCase().includes('failed') &&
          !response?.message?.toLowerCase().includes('error'))
      ) {
        toast.success('Jellyfin connected', 'Connection test successful')
      }

      return response || { status: '500', message: 'Unknown error' }
    },
  }

  const tmdbConnectionTest: ConnectionTestConfig = {
    preSaveKeys: [integrationKeys.tmdbKey],
    saveSpecificSettings,
    testFn: async () => {
      await tmdbTest.refetch()

      const response = tmdbTest.data?.value
      const error = tmdbTest.error?.value

      if (error) {
        toast.error('Failed to connect', error.message)
        return { status: '500', message: error.message }
      }

      if (
        response?.status?.includes('200') ||
        (!response?.message?.toLowerCase().includes('failed') &&
          !response?.message?.toLowerCase().includes('error'))
      ) {
        toast.success('TMDb API key valid', 'Connection test successful')
      }

      return response || { status: '500', message: 'Unknown error' }
    },
  }

  const sonarrConnectionTest: ConnectionTestConfig = {
    preSaveKeys: [integrationKeys.sonarrUrl, integrationKeys.sonarrKey],
    saveSpecificSettings,
    testFn: async () => {
      toast.info('Sonarr API not configured', 'Connection test not available yet')
      return { status: '501', message: 'API not implemented' }
    },
    buttonLabel: 'Check Connection (Not Available)',
  }

  const radarrConnectionTest: ConnectionTestConfig = {
    preSaveKeys: [integrationKeys.radarrUrl, integrationKeys.radarrKey],
    saveSpecificSettings,
    testFn: async () => {
      toast.info('Radarr API not configured', 'Connection test not available yet')
      return { status: '501', message: 'API not implemented' }
    },
    buttonLabel: 'Check Connection (Not Available)',
  }
</script>

<template>
  <section class="settings-section">
    <h2 class="section-title">Integrations</h2>
    <p class="section-description">External service connections and API credentials</p>

    <div class="settings-grid">
      <!-- Jellyfin -->
      <SettingsCard
        title="Jellyfin"
        description="Connect to your Jellyfin media server for syncing"
        :connection-test="jellyfinConnectionTest"
      >
        <SettingUrlInput
          :model-value="getSetting(integrationKeys.jellyfinUrl)"
          label="Base URL"
          :setting-key="integrationKeys.jellyfinUrl"
          placeholder="localhost:8096"
          @update:model-value="(v) => updateSetting(integrationKeys.jellyfinUrl, v)"
        />
        <SettingInput
          :model-value="getSetting(integrationKeys.jellyfinKey)"
          label="API Key"
          :setting-key="integrationKeys.jellyfinKey"
          type="password"
          placeholder="Your Jellyfin API key"
          @update:model-value="(v) => updateSetting(integrationKeys.jellyfinKey, v)"
        />
      </SettingsCard>

      <!-- TMDb -->
      <SettingsCard
        title="TMDb"
        description="The Movie Database API for metadata"
        :connection-test="tmdbConnectionTest"
      >
        <SettingInput
          :model-value="getSetting(integrationKeys.tmdbKey)"
          label="API Key"
          :setting-key="integrationKeys.tmdbKey"
          type="password"
          placeholder="Your TMDb API key"
          @update:model-value="(v) => updateSetting(integrationKeys.tmdbKey, v)"
        />
      </SettingsCard>

      <!-- Sonarr -->
      <SettingsCard
        title="Sonarr"
        description="Connect to Sonarr for series management"
        :connection-test="sonarrConnectionTest"
      >
        <SettingUrlInput
          :model-value="getSetting(integrationKeys.sonarrUrl)"
          label="Base URL"
          :setting-key="integrationKeys.sonarrUrl"
          placeholder="localhost:8989"
          @update:model-value="(v) => updateSetting(integrationKeys.sonarrUrl, v)"
        />
        <SettingInput
          :model-value="getSetting(integrationKeys.sonarrKey)"
          label="API Key"
          :setting-key="integrationKeys.sonarrKey"
          type="password"
          placeholder="Your Sonarr API key"
          @update:model-value="(v) => updateSetting(integrationKeys.sonarrKey, v)"
        />
      </SettingsCard>

      <!-- Radarr -->
      <SettingsCard
        title="Radarr"
        description="Connect to Radarr for movie management"
        :connection-test="radarrConnectionTest"
      >
        <SettingUrlInput
          :model-value="getSetting(integrationKeys.radarrUrl)"
          label="Base URL"
          :setting-key="integrationKeys.radarrUrl"
          placeholder="localhost:7878"
          @update:model-value="(v) => updateSetting(integrationKeys.radarrUrl, v)"
        />
        <SettingInput
          :model-value="getSetting(integrationKeys.radarrKey)"
          label="API Key"
          :setting-key="integrationKeys.radarrKey"
          type="password"
          placeholder="Your Radarr API key"
          @update:model-value="(v) => updateSetting(integrationKeys.radarrKey, v)"
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
