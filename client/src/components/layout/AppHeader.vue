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
        <!-- AI Gen SVG Logo, does the job on small size -->
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 140" width="64" height="50">
          <!-- Tail (draw first, behind body) -->
          <path
            d="M 38 95 Q 15 90 10 70 Q 5 48 20 36"
            fill="none"
            stroke="#6B4F9E"
            stroke-width="6"
            stroke-linecap="round"
          />

          <!-- Star at tail tip -->
          <polygon
            points="20,22 22.9,31 32,31 24.5,36.5 27.4,45.5 20,40 12.6,45.5 15.5,36.5 8,31 17.1,31"
            fill="#F5C518"
            stroke="#D4A800"
            stroke-width="0.8"
          />

          <!-- Body -->
          <ellipse cx="75" cy="88" rx="36" ry="24" fill="#9B7FBE" />

          <!-- Belly -->
          <ellipse cx="82" cy="92" rx="20" ry="14" fill="#F5E6C8" />

          <!-- Head -->
          <circle cx="112" cy="72" r="22" fill="#9B7FBE" />

          <!-- Ear left -->
          <ellipse cx="103" cy="52" rx="8" ry="11" fill="#9B7FBE" transform="rotate(-15 103 52)" />
          <ellipse cx="103" cy="53" rx="4.5" ry="7" fill="#E87BA0" transform="rotate(-15 103 53)" />

          <!-- Ear right -->
          <ellipse cx="119" cy="49" rx="8" ry="11" fill="#9B7FBE" transform="rotate(10 119 49)" />
          <ellipse cx="119" cy="50" rx="4.5" ry="7" fill="#E87BA0" transform="rotate(10 119 50)" />

          <!-- Eye -->
          <circle cx="124" cy="68" r="5" fill="#1A1A2E" />
          <circle cx="125.5" cy="66.5" r="1.8" fill="white" />

          <!-- Nose -->
          <ellipse cx="134" cy="76" rx="4" ry="3" fill="#E87BA0" />

          <!-- Whiskers top -->
          <line
            x1="133"
            y1="73"
            x2="152"
            y2="68"
            stroke="#6B4F9E"
            stroke-width="1.2"
            stroke-linecap="round"
          />
          <line
            x1="133"
            y1="75"
            x2="153"
            y2="74"
            stroke="#6B4F9E"
            stroke-width="1.2"
            stroke-linecap="round"
          />
          <!-- Whiskers bottom -->
          <line
            x1="133"
            y1="78"
            x2="152"
            y2="80"
            stroke="#6B4F9E"
            stroke-width="1.2"
            stroke-linecap="round"
          />
          <line
            x1="133"
            y1="80"
            x2="151"
            y2="86"
            stroke="#6B4F9E"
            stroke-width="1.2"
            stroke-linecap="round"
          />

          <!-- Teeth -->
          <rect
            x="127"
            y="82"
            width="5"
            height="7"
            rx="1.5"
            fill="#FFFDF0"
            stroke="#D4D0C0"
            stroke-width="0.5"
          />
          <rect
            x="133"
            y="82"
            width="5"
            height="7"
            rx="1.5"
            fill="#FFFDF0"
            stroke="#D4D0C0"
            stroke-width="0.5"
          />

          <!-- Front legs -->
          <ellipse cx="95" cy="108" rx="8" ry="5" fill="#7B5EA7" />
          <ellipse cx="108" cy="110" rx="8" ry="5" fill="#7B5EA7" />

          <!-- Hind legs -->
          <ellipse cx="58" cy="107" rx="9" ry="5" fill="#7B5EA7" />
          <ellipse cx="44" cy="106" rx="8" ry="5" fill="#7B5EA7" />
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
