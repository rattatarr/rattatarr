<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Button from 'primevue/button'
  import { Icon, MediaType } from '@/utils/enums'
  import CastCard from './CastCard.vue'
  import { PersonFilterDialog } from '@/components/common'
  import type { CastMember, Person } from '@/types'

  interface Props {
    title?: string
    originalTitle?: string
    description?: string
    cast?: CastMember[]
    imdbId?: string
    tmdbId?: string
    mediaType?: MediaType.MOVIE | MediaType.SERIES
  }

  const props = withDefaults(defineProps<Props>(), {
    mediaType: MediaType.MOVIE,
  })

  const showAllCast = ref(false)

  const selectedPerson = ref<Person | null>(null)
  const personDialogVisible = ref(false)

  function openPersonDialog(
    person?: { id?: string; name?: string | null; profilePathUrl?: string | null } | null,
  ) {
    if (!person?.id) return
    selectedPerson.value = {
      id: person.id,
      name: person.name ?? undefined,
      profilePathUrl: person.profilePathUrl ?? undefined,
    }
    personDialogVisible.value = true
  }

  const sortedCast = computed(() => {
    if (!props.cast) return []
    return [...props.cast].sort((a, b) => (a.order || 0) - (b.order || 0))
  })

  const displayedCast = computed(() => {
    if (showAllCast.value) return sortedCast.value
    return sortedCast.value.slice(0, 6)
  })

  const hasAdditionalInfo = computed(() => {
    return (
      (props.originalTitle && props.originalTitle !== props.title) || props.imdbId || props.tmdbId
    )
  })

  const tmdbPath = computed(() => (props.mediaType === MediaType.SERIES ? 'tv' : props.mediaType))
</script>

<template>
  <div class="media-metadata">
    <!-- Description -->
    <div v-if="description" class="metadata-section">
      <h2 class="section-title">Overview</h2>
      <p class="description">{{ description }}</p>
    </div>

    <!-- Cast -->
    <div v-if="cast && cast.length > 0" class="metadata-section">
      <div class="section-header">
        <h2 class="section-title">
          <i :class="Icon.USERS" />
          Cast
        </h2>
        <Button
          v-if="cast.length > 6"
          :label="showAllCast ? 'Show Less' : `Show All (${cast.length})`"
          :icon="showAllCast ? Icon.CHEVRON_UP : Icon.CHEVRON_DOWN"
          text
          size="small"
          @click="showAllCast = !showAllCast"
        />
      </div>
      <div class="cast-grid">
        <CastCard
          v-for="member in displayedCast"
          :key="member.id"
          :name="member.person?.name || 'Unknown'"
          :profile-url="member.person?.profilePathUrl"
          :character="member.character"
          class="cast-card-clickable"
          @click="openPersonDialog(member.person)"
        />
      </div>
    </div>

    <!-- Person Filter Dialog -->
    <PersonFilterDialog v-model:visible="personDialogVisible" :person="selectedPerson" />

    <!-- Crew (passed from parent) -->
    <slot name="crew" />

    <!-- Additional Info -->
    <div v-if="hasAdditionalInfo" class="metadata-section">
      <h2 class="section-title">Details</h2>
      <div class="details-grid">
        <div v-if="originalTitle && originalTitle !== title" class="detail-row">
          <span class="detail-label">Original Title:</span>
          <span class="detail-value">{{ originalTitle }}</span>
        </div>
        <div v-if="imdbId" class="detail-row">
          <span class="detail-label">IMDb:</span>
          <a
            :href="`https://www.imdb.com/title/${imdbId}`"
            target="_blank"
            rel="noopener noreferrer"
            class="detail-link"
          >
            {{ imdbId }}
            <i :class="Icon.EXTERNAL_LINK" />
          </a>
        </div>
        <div v-if="tmdbId" class="detail-row">
          <span class="detail-label">TMDb:</span>
          <a
            :href="`https://www.themoviedb.org/${tmdbPath}/${tmdbId}`"
            target="_blank"
            rel="noopener noreferrer"
            class="detail-link"
          >
            {{ tmdbId }}
            <i :class="Icon.EXTERNAL_LINK" />
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .media-metadata {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }

  .metadata-section {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--p-text-color);
    margin: 0;
  }

  .section-title i {
    font-size: 1.25rem;
    color: var(--primary-color);
  }

  .description {
    font-size: 1rem;
    line-height: 1.6;
    color: var(--p-text-color-secondary);
    margin: 0;
  }

  .cast-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1rem;
  }

  .cast-card-clickable {
    cursor: pointer;
  }

  .details-grid {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .detail-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem 0;
  }

  .detail-label {
    font-weight: 600;
    color: var(--p-text-color);
    min-width: 120px;
  }

  .detail-value {
    color: var(--p-text-color-secondary);
  }

  .detail-link {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--primary-color);
    text-decoration: none;
    transition: color 0.2s;
  }

  .detail-link:hover {
    color: var(--primary-color-emphasis);
    text-decoration: underline;
  }

  .detail-link i {
    font-size: 0.875rem;
  }

  @media (max-width: 768px) {
    .cast-grid {
      grid-template-columns: 1fr;
    }

    .section-header {
      flex-direction: column;
      align-items: flex-start;
    }

    .detail-row {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.25rem;
    }

    .detail-label {
      min-width: auto;
    }
  }
</style>
