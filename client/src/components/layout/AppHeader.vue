<script setup lang="ts">
  import Toolbar from 'primevue/toolbar'
  import InputText from 'primevue/inputtext'
  import IconField from 'primevue/iconfield'
  import InputIcon from 'primevue/inputicon'
  import Select from 'primevue/select'
  import Avatar from 'primevue/avatar'
  import { useProfileManagement, useMediaSearch, useNavigation } from '@/composables'
  import { Icon } from '@/utils/enums'

  const { profileList, selectedProfile, isLoading, onProfileChange } = useProfileManagement()
  const { searchQuery } = useMediaSearch()
  const { goHome } = useNavigation()
</script>

<template>
  <Toolbar class="app-header">
    <!-- Left: Logo + App Name -->
    <template #start>
      <div class="app-header__brand" @click="goHome">
        <!-- Dummy SVG Logo -->
        <svg
          width="32"
          height="32"
          viewBox="0 0 32 32"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          class="app-header__logo"
        >
          <rect width="32" height="32" rx="6" fill="var(--primary-color)" />
          <path
            d="M12 10L16 14L20 10M12 22L16 18L20 22M10 12L14 16L10 20M22 12L18 16L22 20"
            stroke="white"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span class="app-header__title">Rattatarr</span>
      </div>
    </template>

    <!-- Center: Global Search -->
    <template #center>
      <IconField class="app-header__search">
        <InputIcon :class="Icon.SEARCH" />
        <InputText v-model="searchQuery" placeholder="Search movies, series..." />
      </IconField>
    </template>

    <!-- Right: Profile Selector -->
    <template #end>
      <Select
        v-model="selectedProfile"
        :options="profileList"
        option-label="name"
        placeholder="Select Profile"
        class="app-header__profile-selector"
        :loading="isLoading"
        @change="onProfileChange"
      >
        <template #value="{ value }">
          <div v-if="value" class="profile-dropdown-value">
            <Avatar :label="value.name[0].toUpperCase()" shape="circle" />
            <span>{{ value.name }}</span>
          </div>
          <span v-else>Select Profile</span>
        </template>
        <template #option="{ option }">
          <div class="profile-dropdown-option">
            <Avatar :label="option.name[0].toUpperCase()" shape="circle" />
            <span>{{ option.name }}</span>
          </div>
        </template>
      </Select>
    </template>
  </Toolbar>
</template>

<style scoped>
  .app-header {
    position: relative;
    border: none !important;
    border-radius: 0 !important;
    background: transparent !important;
    box-shadow: none !important;
    padding: 0.75rem 1.5rem;
  }

  /* Border only on bottom, starting after sidebar (250px) */
  .app-header::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 250px;
    right: 0;
    height: 1px;
    background: rgba(255, 255, 255, 0.08);
  }

  .app-header__brand {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    cursor: pointer;
    user-select: none;
  }

  .app-header__logo {
    flex-shrink: 0;
  }

  .app-header__title {
    font-size: 1.25rem;
    font-weight: 600;
  }

  .app-header__search {
    width: 100%;
    max-width: 500px;
  }

  .app-header__profile-selector {
    min-width: 200px;
  }

  .profile-dropdown-value,
  .profile-dropdown-option {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
</style>
