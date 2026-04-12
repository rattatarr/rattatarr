<script setup lang="ts">
  import { ref } from 'vue'
  import Menu from 'primevue/menu'
  import { navigationItems } from '@/config/navigation'
  import { getAppVersion } from '@/utils'

  const menuItems = ref(navigationItems)
  const appVersion = getAppVersion()
</script>

<template>
  <aside class="app-sidebar">
    <div class="app-sidebar__content">
      <Menu :model="menuItems" class="app-sidebar__menu">
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
  </aside>
</template>

<style scoped>
  .app-sidebar {
    height: 100%;
    display: flex;
    flex-direction: column;
    margin: 0;
    padding: 0;
    position: relative;
  }

  /* Softer border on the right side using gradient */
  .app-sidebar::after {
    content: '';
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 1px;
    background: linear-gradient(
      to bottom,
      transparent,
      rgba(255, 255, 255, 0.08) 0%,
      rgba(255, 255, 255, 0.08) 50%,
      transparent
    );
  }

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

  .app-sidebar__menu {
    width: 100%;
    border: none;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
  }

  .app-sidebar__menu :deep(.p-menu-list) {
    padding: 0.5rem 0;
    background: transparent;
    border: none;
  }

  .app-sidebar__menu :deep(.p-menu-item-content) {
    border-radius: 0;
  }

  .app-sidebar__menu :deep(.router-link-active) {
    background-color: var(--p-primary-color);
    color: var(--p-primary-contrast-color);
  }

  .app-sidebar__menu :deep(a) {
    padding: 1rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .app-sidebar__menu :deep(.p-menu-separator) {
    margin: 0.5rem 0;
  }

  .app-sidebar__footer {
    padding: 1rem 1.5rem;
  }

  .app-sidebar__version {
    font-size: 0.75rem;
    color: var(--p-text-secondary-color);
    opacity: 0.7;
    text-align: center;
    display: block;
  }
</style>
