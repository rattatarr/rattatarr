<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { SettingsCard } from '@/components/settings'
  import { SyncButton, FileUpload } from '@/components/sync'
  import { useSyncJellyfinMedia, useSyncJellyfinProfiles } from '@/queries/useJellyfin'
  import { useImportIMDbRatings, useExportRatingsCsv } from '@/queries/useRatings'
  import { useProfileManagement } from '@/composables/useProfileManagement'
  import { useSyncOperation } from '@/composables/useSyncOperation'
  import { useToast } from '@/composables/useToast'
  import { useSettingsForm } from '@/composables/useSettingsForm'
  import { useProfileStore } from '@/stores/profileStore'
  import type { GenericResponse, ProfilesWrapper } from '@/schemas/types/api'
  import { OperationStatus, ButtonSeverity, Icon } from '@/utils/enums'

  const toast = useToast()
  const { savedSettings } = useSettingsForm()
  const profileStore = useProfileStore()
  const { selectedProfile } = useProfileManagement()

  // Setting keys
  const integrationKeys = {
    jellyfinUrl: 'jellyfin.base_url',
    jellyfinKey: 'jellyfin.api_key',
    tmdbKey: 'tmdb.api_key',
  }

  // Jellyfin Profiles Sync
  const syncJellyfinProfilesMutation = useSyncJellyfinProfiles()
  const jellyfinProfilesSync = useSyncOperation<ProfilesWrapper>({
    mutation: syncJellyfinProfilesMutation,
    successMessage: 'Jellyfin profiles synced',
    successDescription: (data) => `${data.profiles?.length ?? 0} profile(s) imported from Jellyfin`,
    errorMessage: 'Failed to sync Jellyfin profiles',
  })

  // Jellyfin Media Sync
  const syncJellyfinMutation = useSyncJellyfinMedia()
  const jellyfinSync = useSyncOperation<GenericResponse>({
    mutation: syncJellyfinMutation,
    successMessage: 'Jellyfin sync started',
    successDescription: (data) => data.message || 'Media synchronization in progress',
    errorMessage: 'Failed to sync Jellyfin media',
  })

  // IMDb Rating Import
  const importIMDbMutation = useImportIMDbRatings()
  const exportRatingsCsvMutation = useExportRatingsCsv()
  const selectedFile = ref<File | null>(null)

  const imdbImportStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const imdbImportButtonProps = computed(() => {
    switch (imdbImportStatus.value) {
      case OperationStatus.LOADING:
        return { loading: true }
      case OperationStatus.SUCCESS:
        return { severity: ButtonSeverity.SUCCESS, icon: Icon.CHECK, loading: false }
      case OperationStatus.ERROR:
        return { severity: ButtonSeverity.DANGER, icon: Icon.TIMES, loading: false }
      default:
        return { loading: false }
    }
  })

  async function executeImdbImport() {
    if (!selectedFile.value) {
      toast.error('No file selected', { fallbackMessage: 'Please select a CSV file' })
      return
    }

    // Validate file type
    if (!selectedFile.value.name.endsWith('.csv')) {
      toast.error('Invalid file type', { fallbackMessage: 'Please select a CSV file' })
      return
    }

    if (!profileStore.selectedProfileId) {
      toast.error('No profile selected', { fallbackMessage: 'Please select a profile first' })
      return
    }

    imdbImportStatus.value = OperationStatus.LOADING

    try {
      const data = await importIMDbMutation.mutateAsync({
        file: selectedFile.value,
        profileId: profileStore.selectedProfileId,
      })
      imdbImportStatus.value = OperationStatus.SUCCESS

      toast.success('IMDb ratings imported', {
        description: data.message || 'Ratings import successful',
      })

      selectedFile.value = null

      setTimeout(() => {
        imdbImportStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      imdbImportStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to import IMDb ratings' })

      setTimeout(() => {
        imdbImportStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  const canImportIMDb = computed(() => !!selectedFile.value)

  const ratingsExportStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const ratingsExportButtonProps = computed(() => {
    switch (ratingsExportStatus.value) {
      case OperationStatus.LOADING:
        return { loading: true }
      case OperationStatus.SUCCESS:
        return { severity: ButtonSeverity.SUCCESS, icon: Icon.CHECK, loading: false }
      case OperationStatus.ERROR:
        return { severity: ButtonSeverity.DANGER, icon: Icon.TIMES, loading: false }
      default:
        return { loading: false }
    }
  })

  async function executeRatingsExport() {
    if (!selectedProfile.value?.id) {
      toast.error('No profile selected', { fallbackMessage: 'Please select a profile first' })
      return
    }

    ratingsExportStatus.value = OperationStatus.LOADING

    try {
      const { csvData, fileName } = await exportRatingsCsvMutation.mutateAsync(
        selectedProfile.value.id,
      )
      const blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')

      link.href = url
      link.download = fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)

      ratingsExportStatus.value = OperationStatus.SUCCESS
      toast.success('Ratings exported', {
        description: 'CSV download started successfully',
      })

      setTimeout(() => {
        ratingsExportStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      ratingsExportStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to export ratings CSV' })

      setTimeout(() => {
        ratingsExportStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  const canExportRatings = computed(() => !!selectedProfile.value?.id)
</script>

<template>
  <section class="settings-section">
    <h2 class="section-title">External Synchronization</h2>
    <p class="section-description">Sync media and import ratings from external sources</p>

    <div class="settings-grid">
      <!-- Jellyfin Profiles Sync -->
      <SettingsCard
        title="Jellyfin Profile Sync"
        description="Import Jellyfin users as Rattatarr profiles"
      >
        <SyncButton
          label="Sync Jellyfin Profiles"
          :required-settings="[
            { key: integrationKeys.jellyfinUrl, displayName: 'Jellyfin URL' },
            { key: integrationKeys.jellyfinKey, displayName: 'Jellyfin API Key' },
          ]"
          :settings="savedSettings"
          :status="jellyfinProfilesSync.status.value"
          :severity="jellyfinProfilesSync.buttonProps.value.severity"
          :icon="jellyfinProfilesSync.buttonProps.value.icon"
          :loading="jellyfinProfilesSync.buttonProps.value.loading"
          info-message="Syncing profiles will import all Jellyfin users as Rattatarr profiles. Existing profiles will not be removed."
          @click="jellyfinProfilesSync.execute"
        />
      </SettingsCard>

      <!-- Jellyfin Media Sync -->
      <SettingsCard title="Jellyfin Media Sync" description="Sync your media with Rattatarr">
        <SyncButton
          label="Sync Jellyfin Media"
          :required-settings="[
            { key: integrationKeys.jellyfinUrl, displayName: 'Jellyfin URL' },
            { key: integrationKeys.jellyfinKey, displayName: 'Jellyfin API Key' },
            { key: integrationKeys.tmdbKey, displayName: 'TMDb API Key' },
          ]"
          :settings="savedSettings"
          :status="jellyfinSync.status.value"
          :severity="jellyfinSync.buttonProps.value.severity"
          :icon="jellyfinSync.buttonProps.value.icon"
          :loading="jellyfinSync.buttonProps.value.loading"
          info-message="The media sync will be done in background and may take several minutes to complete depending on the size of your library. You'll be notified when this is done."
          @click="jellyfinSync.execute"
        />
      </SettingsCard>

      <!-- IMDb Rating Import -->
      <SettingsCard title="IMDb Rating Import">
        <p class="card-description-custom">
          Go to
          <a
            href="https://www.imdb.com/exports/"
            target="_blank"
            rel="noopener noreferrer"
            class="imdb-link"
          >
            https://www.imdb.com/exports/
          </a>
          and grab your export CSV file
        </p>

        <div class="imdb-import-content">
          <FileUpload v-model="selectedFile" accept=".csv" placeholder="Choose CSV File" />

          <SyncButton
            label="Import Ratings"
            :required-settings="[{ key: integrationKeys.tmdbKey, displayName: 'TMDb API Key' }]"
            :settings="savedSettings"
            :status="imdbImportStatus"
            :severity="imdbImportButtonProps.severity"
            :icon="imdbImportButtonProps.icon"
            :loading="imdbImportButtonProps.loading"
            :disabled="!canImportIMDb"
            info-message="The rating import will be processed in background and may take a few minutes to complete depending on the file size. You'll be notified when this is done."
            @click="executeImdbImport"
          />
        </div>
      </SettingsCard>

      <!-- Ratings CSV Export -->
      <SettingsCard title="Ratings CSV Export" description="Download profile ratings as CSV">
        <SyncButton
          label="Export Ratings CSV"
          :status="ratingsExportStatus"
          :severity="ratingsExportButtonProps.severity"
          :icon="ratingsExportButtonProps.icon"
          :loading="ratingsExportButtonProps.loading"
          :disabled="!canExportRatings"
          info-message="Exports ratings for selected profile and downloads a CSV file to your device."
          @click="executeRatingsExport"
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

  .card-description-custom {
    color: var(--p-text-secondary-color);
    margin: 0 0 1.25rem 0;
    font-size: 0.875rem;
    line-height: 1.5;
  }

  .imdb-link {
    color: var(--p-primary-color);
    text-decoration: none;
    font-weight: 500;
  }

  .imdb-link:hover {
    text-decoration: underline;
  }

  .imdb-import-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  @media (max-width: 768px) {
    .settings-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
