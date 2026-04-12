<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Card from 'primevue/card'
  import Button from 'primevue/button'
  import { PersonFilterDialog } from '@/components/common'
  import type { PersonStat, Person } from '@/types'

  interface Props {
    title: string
    byCount: PersonStat[]
    byScore: PersonStat[]
  }

  const props = defineProps<Props>()

  type ViewMode = 'count' | 'score'
  const viewMode = ref<ViewMode>('count')

  const activePeople = computed<PersonStat[]>(() =>
    viewMode.value === 'count' ? props.byCount : props.byScore,
  )

  const selectedPerson = ref<Person | null>(null)
  const personDialogVisible = ref(false)

  function openPersonDialog(stat: PersonStat) {
    if (!stat.personId) return
    selectedPerson.value = {
      id: stat.personId,
      name: stat.name,
      profilePathUrl: stat.profilePathUrl,
      TMDbId: undefined,
    }
    personDialogVisible.value = true
  }
</script>

<template>
  <Card class="people-card">
    <template #title>
      <div class="card-header">
        <span>{{ title }}</span>
        <div class="toggle-buttons">
          <Button
            label="Count"
            size="small"
            :severity="viewMode === 'count' ? 'primary' : 'secondary'"
            @click="viewMode = 'count'"
          />
          <Button
            label="Score"
            size="small"
            :severity="viewMode === 'score' ? 'primary' : 'secondary'"
            @click="viewMode = 'score'"
          />
        </div>
      </div>
    </template>
    <template #content>
      <div class="people-list">
        <div
          v-for="person in activePeople"
          :key="person.personId ?? ''"
          class="person-row person-row-clickable"
          @click="openPersonDialog(person)"
        >
          <div class="avatar-wrapper">
            <img
              v-if="person.profilePathUrl"
              :src="person.profilePathUrl"
              :alt="person.name ?? ''"
              class="person-avatar-img"
            />
            <div v-else class="person-avatar-fallback">
              {{ person.name?.[0] ?? '?' }}
            </div>
          </div>
          <div class="person-info">
            <span class="person-name">{{ person.name }}</span>
            <span class="person-count">{{ person.itemCount }} titles</span>
          </div>
          <span class="person-rating">{{ person.averageRating?.toFixed(2) }}</span>
        </div>
      </div>
    </template>
  </Card>

  <PersonFilterDialog v-model:visible="personDialogVisible" :person="selectedPerson" />
</template>

<style scoped>
  .people-card {
    height: 100%;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    flex-wrap: wrap;
  }

  .toggle-buttons {
    display: flex;
    gap: 0.25rem;
  }

  .people-list {
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
    max-height: 380px;
    overflow-y: auto;
    padding-right: 0.5rem;
    scrollbar-gutter: stable;
  }

  .person-row {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    padding: 0.3rem 0;
    border-bottom: 1px solid var(--p-surface-200);
  }

  .person-row-clickable {
    cursor: pointer;
    border-radius: var(--border-radius);
    transition: background-color 0.2s;
  }

  .person-row-clickable:hover {
    background-color: var(--p-surface-800);
  }

  .person-row:last-child {
    border-bottom: none;
  }

  .avatar-wrapper {
    flex-shrink: 0;
    width: 2.25rem;
    height: 2.25rem;
    border-radius: 50%;
    overflow: hidden;
    background: var(--p-surface-300);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .person-avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .person-avatar-fallback {
    font-size: 1rem;
    font-weight: 600;
    color: var(--p-text-color);
    text-transform: uppercase;
  }

  .person-info {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
  }

  .person-name {
    font-size: 0.875rem;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .person-count {
    font-size: 0.72rem;
    color: var(--p-text-muted-color);
  }

  .person-rating {
    font-size: 1rem;
    font-weight: 700;
    color: var(--p-primary-color);
    white-space: nowrap;
  }
</style>
