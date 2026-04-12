<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Button from 'primevue/button'
  import Avatar from 'primevue/avatar'
  import { Icon, getInitials } from '@/utils'
  import { PersonFilterDialog } from '@/components/common'
  import type { CrewMember, Person } from '@/types'

  interface Props {
    crew?: CrewMember[]
  }

  const props = defineProps<Props>()

  const showAllCrew = ref(false)
  const imageErrors = ref(new Set<string>())

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

  const directors = computed(() => {
    if (!props.crew) return []
    return props.crew.filter(
      (member) => member.job?.toLowerCase() === 'director' && member.department === 'Directing',
    )
  })

  const producers = computed(() => {
    if (!props.crew) return []
    return props.crew.filter(
      (member) => member.job?.toLowerCase() === 'producer' && member.department === 'Production',
    )
  })

  const writers = computed(() => {
    if (!props.crew) return []
    return props.crew.filter((member) => member.department === 'Writing')
  })

  const pinnedIds = computed(() => {
    const ids = new Set<string | undefined>()
    directors.value.forEach((m) => ids.add(m.id))
    producers.value.forEach((m) => ids.add(m.id))
    writers.value.forEach((m) => ids.add(m.id))
    return ids
  })

  const otherCrew = computed(() => {
    if (!props.crew) return []
    return props.crew.filter((member) => !pinnedIds.value.has(member.id))
  })

  const displayedOtherCrew = computed(() => {
    return otherCrew.value
  })

  const groupedDepartments = computed(() => {
    const groups = new Map<string, CrewMember[]>()

    displayedOtherCrew.value.forEach((member) => {
      const dept = member.department || 'Other'
      if (!groups.has(dept)) {
        groups.set(dept, [])
      }
      groups.get(dept)?.push(member)
    })

    return Array.from(groups.entries()).map(([name, members]) => ({
      name,
      members,
    }))
  })

  const handleImageError = (_event: Event, memberId?: string) => {
    if (memberId) {
      imageErrors.value.add(memberId)
    }
  }
</script>

<template>
  <div v-if="crew && crew.length > 0" class="crew-section">
    <div class="section-header">
      <h2 class="section-title">
        <i :class="Icon.USERS" />
        Crew
      </h2>
      <Button
        v-if="otherCrew.length > 0"
        :label="showAllCrew ? 'Show Less' : `Show All (${crew.length})`"
        :icon="showAllCrew ? Icon.CHEVRON_UP : Icon.CHEVRON_DOWN"
        text
        size="small"
        @click="showAllCrew = !showAllCrew"
      />
    </div>

    <!-- Directors (always show) -->
    <div v-if="directors.length > 0" class="crew-category">
      <div class="category-title">
        {{ directors.length > 1 ? 'Directors' : 'Director' }}
      </div>
      <div class="crew-grid">
        <div
          v-for="director in directors"
          :key="director.id"
          class="crew-member crew-member-clickable"
          @click="openPersonDialog(director.person)"
        >
          <div class="avatar-wrapper">
            <img
              v-if="director.person?.profilePathUrl && !imageErrors.has(director.id || '')"
              :src="director.person.profilePathUrl"
              :alt="director.person.name"
              class="crew-avatar-image"
              @error="handleImageError($event, director.id)"
            />
            <Avatar
              v-else
              :label="getInitials(director.person?.name)"
              class="crew-avatar-fallback"
              shape="circle"
              size="large"
            />
          </div>
          <div class="crew-info">
            <div class="crew-name">{{ director.person?.name || 'Unknown' }}</div>
            <div class="crew-job">Director</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Writers (always show) -->
    <div v-if="writers.length > 0" class="crew-category">
      <div class="category-title">
        {{ writers.length > 1 ? 'Writers' : 'Writer' }}
      </div>
      <div class="crew-grid">
        <div
          v-for="member in writers"
          :key="member.id"
          class="crew-member crew-member-clickable"
          @click="openPersonDialog(member.person)"
        >
          <div class="avatar-wrapper">
            <img
              v-if="member.person?.profilePathUrl && !imageErrors.has(member.id || '')"
              :src="member.person.profilePathUrl"
              :alt="member.person.name"
              class="crew-avatar-image"
              @error="handleImageError($event, member.id)"
            />
            <Avatar
              v-else
              :label="getInitials(member.person?.name)"
              class="crew-avatar-fallback"
              shape="circle"
              size="large"
            />
          </div>
          <div class="crew-info">
            <div class="crew-name">{{ member.person?.name || 'Unknown' }}</div>
            <div class="crew-job">{{ member.job }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Producers (always show) -->
    <div v-if="producers.length > 0" class="crew-category">
      <div class="category-title">
        {{ producers.length > 1 ? 'Producers' : 'Producer' }}
      </div>
      <div class="crew-grid">
        <div
          v-for="member in producers"
          :key="member.id"
          class="crew-member crew-member-clickable"
          @click="openPersonDialog(member.person)"
        >
          <div class="avatar-wrapper">
            <img
              v-if="member.person?.profilePathUrl && !imageErrors.has(member.id || '')"
              :src="member.person.profilePathUrl"
              :alt="member.person.name"
              class="crew-avatar-image"
              @error="handleImageError($event, member.id)"
            />
            <Avatar
              v-else
              :label="getInitials(member.person?.name)"
              class="crew-avatar-fallback"
              shape="circle"
              size="large"
            />
          </div>
          <div class="crew-info">
            <div class="crew-name">{{ member.person?.name || 'Unknown' }}</div>
            <div class="crew-job">{{ member.job }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Other Crew (grouped by department, shown only when expanded) -->
    <div v-if="showAllCrew && displayedOtherCrew.length > 0" class="other-crew">
      <div v-for="dept in groupedDepartments" :key="dept.name" class="crew-category">
        <div class="category-title">{{ dept.name }}</div>
        <div class="crew-grid">
          <div
            v-for="member in dept.members"
            :key="member.id"
            class="crew-member crew-member-clickable"
            @click="openPersonDialog(member.person)"
          >
            <div class="avatar-wrapper">
              <img
                v-if="member.person?.profilePathUrl && !imageErrors.has(member.id || '')"
                :src="member.person.profilePathUrl"
                :alt="member.person.name"
                class="crew-avatar-image"
                @error="handleImageError($event, member.id)"
              />
              <Avatar
                v-else
                :label="getInitials(member.person?.name)"
                class="crew-avatar-fallback"
                shape="circle"
                size="large"
              />
            </div>
            <div class="crew-info">
              <div class="crew-name">{{ member.person?.name || 'Unknown' }}</div>
              <div class="crew-job">{{ member.job }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Person Filter Dialog -->
  <PersonFilterDialog v-model:visible="personDialogVisible" :person="selectedPerson" />
</template>

<style scoped>
  .crew-section {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
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

  .crew-category {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .category-title {
    font-size: 1.125rem;
    font-weight: 600;
    color: var(--p-text-color);
    padding-left: 0.25rem;
  }

  .crew-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }

  .crew-member {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem;
    border-radius: var(--border-radius);
    transition: background-color 0.2s;
  }

  .crew-member-clickable {
    cursor: pointer;
  }

  .crew-member:hover {
    background-color: var(--surface-hover);
  }

  .avatar-wrapper {
    flex-shrink: 0;
    width: 50px;
    height: 50px;
  }

  .crew-avatar-image {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
  }

  .crew-avatar-fallback {
    width: 100%;
    height: 100%;
  }

  .crew-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    min-width: 0;
  }

  .crew-name {
    font-weight: 600;
    color: var(--p-text-color);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .crew-job {
    font-size: 0.875rem;
    color: var(--p-text-color-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .other-crew {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  @media (max-width: 768px) {
    .crew-grid {
      grid-template-columns: 1fr;
    }

    .section-header {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>
