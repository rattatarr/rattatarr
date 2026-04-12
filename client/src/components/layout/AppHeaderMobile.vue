<script setup lang="ts">
  import { ref, watch } from 'vue'
  import { useRoute } from 'vue-router'
  import Button from 'primevue/button'
  import Drawer from 'primevue/drawer'
  import Menu from 'primevue/menu'
  import InputText from 'primevue/inputtext'
  import IconField from 'primevue/iconfield'
  import InputIcon from 'primevue/inputicon'
  import Select from 'primevue/select'
  import Avatar from 'primevue/avatar'
  import { useProfileManagement, useMediaSearch, useNavigation } from '@/composables'
  import { navigationItems } from '@/config/navigation'
  import { getAppVersion, Icon } from '@/utils'

  const route = useRoute()
  const appVersion = getAppVersion()
  const sidebarVisible = ref(false)

  watch(
    () => route.path,
    () => {
      sidebarVisible.value = false
    },
  )

  const { profileList, selectedProfile, isLoading, onProfileChange } = useProfileManagement()
  const { searchQuery } = useMediaSearch()
  const { goHome } = useNavigation()

  const menuItems = ref(navigationItems)
</script>

<template>
  <div class="app-header-mobile">
    <!-- Top bar: Hamburger | Logo | Profile -->
    <div class="app-header-mobile__top-bar">
      <!-- Hamburger button (left) -->
      <Button
        :icon="Icon.BARS"
        text
        rounded
        aria-label="Menu"
        class="app-header-mobile__hamburger"
        @click="sidebarVisible = true"
      />

      <!-- Logo (center-left) -->
      <div class="app-header-mobile__brand" @click="goHome">
        <svg
          width="32"
          height="32"
          viewBox="0 0 32 32"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          class="app-header-mobile__logo"
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
        <span class="app-header-mobile__title">Rattatarr</span>
      </div>

      <!-- Spacer -->
      <div class="app-header-mobile__spacer"></div>

      <!-- Profile selector (right) -->
      <Select
        v-model="selectedProfile"
        :options="profileList"
        option-label="name"
        placeholder="Profile"
        class="app-header-mobile__profile-selector"
        :loading="isLoading"
        @change="onProfileChange"
      >
        <template #value="{ value }">
          <div v-if="value" class="profile-dropdown-value">
            <Avatar :label="value.name[0].toUpperCase()" shape="circle" size="small" />
            <span class="profile-name">{{ value.name }}</span>
          </div>
          <Avatar v-else :icon="Icon.USER" shape="circle" size="small" />
        </template>
        <template #option="{ option }">
          <div class="profile-dropdown-option">
            <Avatar :label="option.name[0].toUpperCase()" shape="circle" size="small" />
            <span>{{ option.name }}</span>
          </div>
        </template>
      </Select>
    </div>

    <!-- Search bar (full width) -->
    <div class="app-header-mobile__search-bar">
      <IconField class="app-header-mobile__search">
        <InputIcon :class="Icon.SEARCH" />
        <InputText v-model="searchQuery" placeholder="Search movies, series..." />
      </IconField>
    </div>

    <!-- Slide-in navigation sidebar -->
    <Drawer v-model:visible="sidebarVisible" position="left" class="app-header-mobile__sidebar">
      <template #header>
        <div class="sidebar-header">
          <span class="sidebar-title">Rattatarr</span>
        </div>
      </template>

      <div class="mobile-sidebar-layout">
        <div class="app-sidebar__content">
          <Menu :model="menuItems" class="mobile-nav-menu">
            <template #item="{ item, props: itemProps }">
              <router-link
                v-if="item.route"
                v-slot="{ href, navigate, isActive }"
                :to="item.route"
                custom
              >
                <a
                  v-bind="itemProps.action"
                  :href="href"
                  :class="{ 'router-link-active': isActive }"
                  @click="navigate"
                >
                  <span v-if="item.icon" :class="item.icon" />
                  <span class="ml-2">{{ item.label }}</span>
                </a>
              </router-link>
            </template>
          </Menu>
        </div>

        <div class="app-sidebar__footer">
          <span class="app-sidebar__version">Version: {{ appVersion }}</span>
        </div>
      </div>
    </Drawer>
  </div>
</template>

<style scoped>
  .app-sidebar__content {
    flex: 1;
    overflow-y: auto;
    border: none !important;
  }

  .app-sidebar__content :deep(.p-menu) {
    border: none;
    border-radius: 0;
    width: 100%;
    background: transparent;
    box-shadow: none;
  }
  .app-header-mobile__sidebar :deep(.p-sidebar-content) {
    height: 100%;
    display: flex;
    flex-direction: column;
    padding: 0; /* optional, since you want full control */
  }

  .mobile-sidebar-layout {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .app-sidebar__footer {
    margin-top: auto;
    padding: 1rem 1.5rem;

    /* same as your desktop behavior */
    background-color: var(--p-primary-contrast-color);
  }

  .app-sidebar__version {
    font-size: 0.75rem;
    color: var(--p-text-secondary-color);
    opacity: 0.7;
    text-align: center;
    display: block;
  }
  .app-header-mobile {
    display: flex;
    flex-direction: column;
    background-color: var(--p-primary-contrast-color);
  }

  /* Top bar layout */
  .app-header-mobile__top-bar {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1rem;
  }

  .app-header-mobile__hamburger {
    flex-shrink: 0;
  }

  .app-header-mobile__brand {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    user-select: none;
    flex-shrink: 0;
  }

  .app-header-mobile__logo {
    flex-shrink: 0;
  }

  .app-header-mobile__title {
    font-size: 1.1rem;
    font-weight: 600;
  }

  .app-header-mobile__spacer {
    flex: 1;
  }

  .app-header-mobile__profile-selector {
    min-width: auto;
    flex-shrink: 0;
  }

  /* Compact profile display for mobile */
  .profile-dropdown-value {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .profile-dropdown-value .profile-name {
    display: none; /* Hide name on very small screens */
  }

  /* Show profile name on slightly larger mobile screens */
  @media (min-width: 400px) {
    .profile-dropdown-value .profile-name {
      display: inline;
    }
  }

  .profile-dropdown-option {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  /* Search bar below top bar */
  .app-header-mobile__search-bar {
    padding: 0 1rem 1rem 1rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  .app-header-mobile__search {
    width: 100%;
  }

  .app-header-mobile__search :deep(.p-inputtext) {
    width: 100%;
  }

  /* Sidebar styling */
  .app-header-mobile__sidebar {
    width: 250px;
  }

  .sidebar-header {
    display: flex;
    align-items: center;
    width: 100%;
  }

  .sidebar-title {
    font-size: 1.25rem;
    font-weight: 600;
  }

  /* Mobile navigation menu styling */
  .mobile-nav-menu {
    width: 100%;
    border: none !important;
    border-radius: 0 !important;
    background: transparent !important;
    box-shadow: none;
  }

  .mobile-nav-menu :deep(.p-menu-list) {
    padding: 0.5rem 0;
    background: transparent;
    border: none;
  }

  .mobile-nav-menu :deep(.p-menu-item-content) {
    border-radius: 0;
  }

  .mobile-nav-menu :deep(.router-link-active) {
    background-color: var(--p-primary-color);
    color: var(--p-primary-contrast-color);
  }

  .mobile-nav-menu :deep(a) {
    padding: 1rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .mobile-nav-menu :deep(.p-menu-separator) {
    margin: 0.5rem 0;
  }
</style>
