<script setup lang="ts">
  import { ref, computed } from 'vue'
  import { SettingsCard } from '@/components/settings'
  import { SyncButton, FileUpload } from '@/components/sync'
  import {
    useSyncJellyfinMedia,
    useSyncJellyfinProfiles,
    usePollJellyfinActivity,
  } from '@/queries/useJellyfin'
  import { useImportIMDbRatings, useExportRatingsCsv } from '@/queries/useRatings'
  import {
    useImportRadarrMovies,
    useRefreshRadarrRatings,
    useImportRadarrAnimeMovies,
    useRefreshRadarrAnimeRatings,
  } from '@/queries/useRadarr'
  import { useImportSonarrSeries, useImportSonarrAnimeSeries } from '@/queries/useSonarr'
  import { useProfileManagement } from '@/composables/useProfileManagement'
  import { useSyncOperation } from '@/composables/useSyncOperation'
  import { useToast } from '@/composables/useToast'
  import { useSettingsForm } from '@/composables/useSettingsForm'
  import { useProfileStore } from '@/stores/profileStore'
  import { useJobTrackingStore } from '@/stores/jobTrackingStore'
  import type { BackgroundJob, ProfilesWrapper } from '@/schemas/types/api'
  import { OperationStatus, ButtonSeverity, Icon, JOB_TYPE } from '@/utils/enums'

  const toast = useToast()
  const { savedSettings } = useSettingsForm()
  const profileStore = useProfileStore()
  const { selectedProfile } = useProfileManagement()
  const jobTrackingStore = useJobTrackingStore()

  // Setting keys
  const integrationKeys = {
    jellyfinUrl: 'jellyfin.base_url',
    jellyfinKey: 'jellyfin.api_key',
    tmdbKey: 'tmdb.api_key',
    radarrUrl: 'radarr.base_url',
    radarrKey: 'radarr.api_key',
    radarrAnimeUrl: 'radarr.anime.base_url',
    radarrAnimeKey: 'radarr.anime.api_key',
    sonarrUrl: 'sonarr.base_url',
    sonarrKey: 'sonarr.api_key',
    sonarrAnimeUrl: 'sonarr.anime.base_url',
    sonarrAnimeKey: 'sonarr.anime.api_key',
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
  const jellyfinSync = useSyncOperation<BackgroundJob>({
    mutation: syncJellyfinMutation,
    successMessage: 'Jellyfin sync started',
    successDescription: () => 'Media synchronization is running in the background',
    errorMessage: 'Failed to sync Jellyfin media',
    onSuccess: (job) => jobTrackingStore.startTracking(job),
    onError: () => jobTrackingStore.unlockJob(JOB_TYPE.JELLYFIN_SYNC),
  })

  function executeJellyfinSync() {
    jobTrackingStore.lockJob(JOB_TYPE.JELLYFIN_SYNC)
    void jellyfinSync.execute()
  }

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

    jobTrackingStore.lockJob(JOB_TYPE.CSV_IMPORT)
    imdbImportStatus.value = OperationStatus.LOADING

    try {
      const data = await importIMDbMutation.mutateAsync({
        file: selectedFile.value,
        profileId: profileStore.selectedProfileId,
      })
      imdbImportStatus.value = OperationStatus.SUCCESS

      toast.success('IMDb import started', {
        description: 'Rating import is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      selectedFile.value = null

      setTimeout(() => {
        imdbImportStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.CSV_IMPORT)
      imdbImportStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to import IMDb ratings' })

      setTimeout(() => {
        imdbImportStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  const canImportIMDb = computed(() => !!selectedFile.value && !!profileStore.selectedProfileId)

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

  // Jellyfin Activity Poll
  const pollJellyfinActivityMutation = usePollJellyfinActivity()
  const jellyfinActivityPollStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const jellyfinActivityPollButtonProps = computed(() => {
    switch (jellyfinActivityPollStatus.value) {
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

  async function executeJellyfinActivityPoll() {
    jobTrackingStore.lockJob(JOB_TYPE.JELLYFIN_ACTIVITY_POLL)
    jellyfinActivityPollStatus.value = OperationStatus.LOADING

    try {
      const data = await pollJellyfinActivityMutation.mutateAsync()
      jellyfinActivityPollStatus.value = OperationStatus.SUCCESS

      toast.success('Jellyfin activity poll started', {
        description: 'Watch activity polling is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      setTimeout(() => {
        jellyfinActivityPollStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.JELLYFIN_ACTIVITY_POLL)
      jellyfinActivityPollStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to poll Jellyfin activity' })

      setTimeout(() => {
        jellyfinActivityPollStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  // Radarr Import
  const importRadarrMutation = useImportRadarrMovies()
  const radarrImportStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const radarrImportButtonProps = computed(() => {
    switch (radarrImportStatus.value) {
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

  async function executeRadarrImport() {
    jobTrackingStore.lockJob(JOB_TYPE.RADARR_IMPORT)
    radarrImportStatus.value = OperationStatus.LOADING

    try {
      const data = await importRadarrMutation.mutateAsync()
      radarrImportStatus.value = OperationStatus.SUCCESS

      toast.success('Radarr import started', {
        description: 'Movie import is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      setTimeout(() => {
        radarrImportStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.RADARR_IMPORT)
      radarrImportStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to import from Radarr' })

      setTimeout(() => {
        radarrImportStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  // Radarr Anime Import
  const importRadarrAnimeMutation = useImportRadarrAnimeMovies()
  const radarrAnimeImportStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const radarrAnimeImportButtonProps = computed(() => {
    switch (radarrAnimeImportStatus.value) {
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

  async function executeRadarrAnimeImport() {
    jobTrackingStore.lockJob(JOB_TYPE.RADARR_ANIME_IMPORT)
    radarrAnimeImportStatus.value = OperationStatus.LOADING

    try {
      const data = await importRadarrAnimeMutation.mutateAsync()
      radarrAnimeImportStatus.value = OperationStatus.SUCCESS

      toast.success('Radarr Anime import started', {
        description: 'Anime movie import is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      setTimeout(() => {
        radarrAnimeImportStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.RADARR_ANIME_IMPORT)
      radarrAnimeImportStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to import from Radarr Anime' })

      setTimeout(() => {
        radarrAnimeImportStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  // Radarr Anime Ratings Refresh
  const refreshRadarrAnimeRatingsMutation = useRefreshRadarrAnimeRatings()
  const radarrAnimeRatingsRefreshStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const radarrAnimeRatingsRefreshButtonProps = computed(() => {
    switch (radarrAnimeRatingsRefreshStatus.value) {
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

  async function executeRadarrAnimeRatingsRefresh() {
    jobTrackingStore.lockJob(JOB_TYPE.RADARR_ANIME_RATINGS_REFRESH)
    radarrAnimeRatingsRefreshStatus.value = OperationStatus.LOADING

    try {
      const data = await refreshRadarrAnimeRatingsMutation.mutateAsync()
      radarrAnimeRatingsRefreshStatus.value = OperationStatus.SUCCESS

      toast.success('Radarr Anime ratings refresh started', {
        description: 'Anime ratings refresh is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      setTimeout(() => {
        radarrAnimeRatingsRefreshStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.RADARR_ANIME_RATINGS_REFRESH)
      radarrAnimeRatingsRefreshStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to refresh Radarr Anime ratings' })

      setTimeout(() => {
        radarrAnimeRatingsRefreshStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  // Sonarr Import
  const importSonarrMutation = useImportSonarrSeries()
  const sonarrImportStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const sonarrImportButtonProps = computed(() => {
    switch (sonarrImportStatus.value) {
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

  async function executeSonarrImport() {
    jobTrackingStore.lockJob(JOB_TYPE.SONARR_IMPORT)
    sonarrImportStatus.value = OperationStatus.LOADING

    try {
      const data = await importSonarrMutation.mutateAsync()
      sonarrImportStatus.value = OperationStatus.SUCCESS

      toast.success('Sonarr import started', {
        description: 'Series import is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      setTimeout(() => {
        sonarrImportStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.SONARR_IMPORT)
      sonarrImportStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to import from Sonarr' })

      setTimeout(() => {
        sonarrImportStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  // Sonarr Anime Import
  const importSonarrAnimeMutation = useImportSonarrAnimeSeries()
  const sonarrAnimeImportStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const sonarrAnimeImportButtonProps = computed(() => {
    switch (sonarrAnimeImportStatus.value) {
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

  async function executeSonarrAnimeImport() {
    jobTrackingStore.lockJob(JOB_TYPE.SONARR_ANIME_IMPORT)
    sonarrAnimeImportStatus.value = OperationStatus.LOADING

    try {
      const data = await importSonarrAnimeMutation.mutateAsync()
      sonarrAnimeImportStatus.value = OperationStatus.SUCCESS

      toast.success('Sonarr Anime import started', {
        description: 'Anime series import is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      setTimeout(() => {
        sonarrAnimeImportStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.SONARR_ANIME_IMPORT)
      sonarrAnimeImportStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to import from Sonarr Anime' })

      setTimeout(() => {
        sonarrAnimeImportStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }

  // Radarr Ratings Refresh
  const refreshRadarrRatingsMutation = useRefreshRadarrRatings()
  const radarrRatingsRefreshStatus = ref<OperationStatus>(OperationStatus.IDLE)
  const radarrRatingsRefreshButtonProps = computed(() => {
    switch (radarrRatingsRefreshStatus.value) {
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

  async function executeRadarrRatingsRefresh() {
    jobTrackingStore.lockJob(JOB_TYPE.RADARR_RATINGS_REFRESH)
    radarrRatingsRefreshStatus.value = OperationStatus.LOADING

    try {
      const data = await refreshRadarrRatingsMutation.mutateAsync()
      radarrRatingsRefreshStatus.value = OperationStatus.SUCCESS

      toast.success('Radarr ratings refresh started', {
        description: 'Ratings refresh is running in the background',
      })

      void jobTrackingStore.startTracking(data)

      setTimeout(() => {
        radarrRatingsRefreshStatus.value = OperationStatus.IDLE
      }, 3000)
    } catch (error) {
      jobTrackingStore.unlockJob(JOB_TYPE.RADARR_RATINGS_REFRESH)
      radarrRatingsRefreshStatus.value = OperationStatus.ERROR
      toast.error(error as Error, { fallbackMessage: 'Failed to refresh Radarr ratings' })

      setTimeout(() => {
        radarrRatingsRefreshStatus.value = OperationStatus.IDLE
      }, 3000)
    }
  }
</script>

<template>
  <section class="settings-section">
    <h2 class="section-title">External Synchronization</h2>
    <p class="section-description">Sync media and import ratings from external sources</p>

    <!-- Row 1: Jellyfin + IMDb + CSV -->
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
          :loading="
            jellyfinSync.buttonProps.value.loading || jobTrackingStore.isJellyfinSyncRunning
          "
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="The media sync will be done in background and may take several minutes to complete depending on the size of your library. You'll be notified when this is done."
          @click="executeJellyfinSync"
        />
      </SettingsCard>

      <!-- Jellyfin Activity Poll -->
      <SettingsCard
        title="Jellyfin Activity Poll"
        description="Poll Jellyfin for recent watch activity"
      >
        <SyncButton
          label="Poll Activity"
          :required-settings="[
            { key: integrationKeys.jellyfinUrl, displayName: 'Jellyfin URL' },
            { key: integrationKeys.jellyfinKey, displayName: 'Jellyfin API Key' },
          ]"
          :settings="savedSettings"
          :status="jellyfinActivityPollStatus"
          :severity="jellyfinActivityPollButtonProps.severity"
          :icon="jellyfinActivityPollButtonProps.icon"
          :loading="
            jellyfinActivityPollButtonProps.loading ||
            jobTrackingStore.isJellyfinActivityPollRunning
          "
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="Polls Jellyfin for new watch activity and records it in Rattatarr. Runs in the background — you will be notified when complete."
          @click="executeJellyfinActivityPoll"
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
            :loading="imdbImportButtonProps.loading || jobTrackingStore.isImdbImportRunning"
            :disabled="!canImportIMDb || jobTrackingStore.isAnyJobRunning"
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

    <!-- Row 2: Radarr + Sonarr -->
    <h3 class="subsection-title">Arr Integrations</h3>
    <div class="settings-grid">
      <!-- Radarr Movie Import -->
      <SettingsCard
        title="Radarr Movie Import"
        description="Look up movies in Radarr's internal database and import them into your library"
      >
        <SyncButton
          label="Import from Radarr"
          :required-settings="[
            { key: integrationKeys.radarrUrl, displayName: 'Radarr URL' },
            { key: integrationKeys.radarrKey, displayName: 'Radarr API Key' },
          ]"
          :settings="savedSettings"
          :status="radarrImportStatus"
          :severity="radarrImportButtonProps.severity"
          :icon="radarrImportButtonProps.icon"
          :loading="radarrImportButtonProps.loading || jobTrackingStore.isRadarrImportRunning"
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="Scans Radarr's internal database for movies and imports any that are not yet in your Rattatarr library. Runs in the background — you will be notified when complete."
          @click="executeRadarrImport"
        />
      </SettingsCard>

      <!-- Radarr Ratings Refresh -->
      <SettingsCard
        title="Radarr Ratings Refresh"
        description="Fetch IMDb and Rotten Tomatoes ratings for all Rattatarr movies via Radarr"
      >
        <SyncButton
          label="Refresh Ratings"
          :required-settings="[
            { key: integrationKeys.radarrUrl, displayName: 'Radarr URL' },
            { key: integrationKeys.radarrKey, displayName: 'Radarr API Key' },
          ]"
          :settings="savedSettings"
          :status="radarrRatingsRefreshStatus"
          :severity="radarrRatingsRefreshButtonProps.severity"
          :icon="radarrRatingsRefreshButtonProps.icon"
          :loading="
            radarrRatingsRefreshButtonProps.loading ||
            jobTrackingStore.isRadarrRatingsRefreshRunning
          "
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="Goes through all movies in your Rattatarr library and updates their IMDb and Rotten Tomatoes ratings using data from Radarr. Runs in the background — you will be notified when complete."
          @click="executeRadarrRatingsRefresh"
        />
      </SettingsCard>

      <!-- Sonarr Series Import -->
      <SettingsCard
        title="Sonarr Series Import"
        description="Look up series in Sonarr's internal database and import them into your library"
      >
        <SyncButton
          label="Import from Sonarr"
          :required-settings="[
            { key: integrationKeys.sonarrUrl, displayName: 'Sonarr URL' },
            { key: integrationKeys.sonarrKey, displayName: 'Sonarr API Key' },
          ]"
          :settings="savedSettings"
          :status="sonarrImportStatus"
          :severity="sonarrImportButtonProps.severity"
          :icon="sonarrImportButtonProps.icon"
          :loading="sonarrImportButtonProps.loading || jobTrackingStore.isSonarrImportRunning"
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="Scans Sonarr's internal database for series and imports any that are not yet in your Rattatarr library. Runs in the background — you will be notified when complete."
          @click="executeSonarrImport"
        />
      </SettingsCard>
    </div>

    <!-- Row 3: Anime Arr -->
    <h3 class="subsection-title">Anime Arr Integrations</h3>
    <div class="settings-grid">
      <!-- Radarr Anime Movie Import -->
      <SettingsCard
        title="Radarr Anime Movie Import"
        description="Import anime movies from your Radarr Anime instance into your library"
      >
        <SyncButton
          label="Import from Radarr Anime"
          :required-settings="[
            { key: integrationKeys.radarrAnimeUrl, displayName: 'Radarr Anime URL' },
            { key: integrationKeys.radarrAnimeKey, displayName: 'Radarr Anime API Key' },
          ]"
          :settings="savedSettings"
          :status="radarrAnimeImportStatus"
          :severity="radarrAnimeImportButtonProps.severity"
          :icon="radarrAnimeImportButtonProps.icon"
          :loading="
            radarrAnimeImportButtonProps.loading || jobTrackingStore.isRadarrAnimeImportRunning
          "
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="Scans your Radarr Anime instance for anime movies and imports any that are not yet in your library. Runs in the background — you will be notified when complete."
          @click="executeRadarrAnimeImport"
        />
      </SettingsCard>

      <!-- Radarr Anime Ratings Refresh -->
      <SettingsCard
        title="Radarr Anime Ratings Refresh"
        description="Fetch IMDb and Rotten Tomatoes ratings for anime movies via Radarr Anime"
      >
        <SyncButton
          label="Refresh Anime Ratings"
          :required-settings="[
            { key: integrationKeys.radarrAnimeUrl, displayName: 'Radarr Anime URL' },
            { key: integrationKeys.radarrAnimeKey, displayName: 'Radarr Anime API Key' },
          ]"
          :settings="savedSettings"
          :status="radarrAnimeRatingsRefreshStatus"
          :severity="radarrAnimeRatingsRefreshButtonProps.severity"
          :icon="radarrAnimeRatingsRefreshButtonProps.icon"
          :loading="
            radarrAnimeRatingsRefreshButtonProps.loading ||
            jobTrackingStore.isRadarrAnimeRatingsRefreshRunning
          "
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="Goes through all anime movies in your library and updates their IMDb and Rotten Tomatoes ratings using data from Radarr Anime. Runs in the background — you will be notified when complete."
          @click="executeRadarrAnimeRatingsRefresh"
        />
      </SettingsCard>

      <!-- Sonarr Anime Series Import -->
      <SettingsCard
        title="Sonarr Anime Series Import"
        description="Import anime series from your Sonarr Anime instance into your library"
      >
        <SyncButton
          label="Import from Sonarr Anime"
          :required-settings="[
            { key: integrationKeys.sonarrAnimeUrl, displayName: 'Sonarr Anime URL' },
            { key: integrationKeys.sonarrAnimeKey, displayName: 'Sonarr Anime API Key' },
          ]"
          :settings="savedSettings"
          :status="sonarrAnimeImportStatus"
          :severity="sonarrAnimeImportButtonProps.severity"
          :icon="sonarrAnimeImportButtonProps.icon"
          :loading="
            sonarrAnimeImportButtonProps.loading || jobTrackingStore.isSonarrAnimeImportRunning
          "
          :disabled="jobTrackingStore.isAnyJobRunning"
          info-message="Scans your Sonarr Anime instance for anime series and imports any that are not yet in your library. Runs in the background — you will be notified when complete."
          @click="executeSonarrAnimeImport"
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

  .subsection-title {
    font-size: 1.125rem;
    font-weight: 600;
    color: var(--p-text-color);
    margin: 1rem 0 0 0;
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
