<script setup lang="ts">
  import { useMediaQuery } from '@vueuse/core'
  import AppHeader from './AppHeader.vue'
  import AppHeaderMobile from './AppHeaderMobile.vue'
  import AppSidebar from './AppSidebar.vue'

  const isMobile = useMediaQuery('(max-width: 768px)')
</script>

<template>
  <div class="app-layout">
    <!-- Header: Mobile vs Desktop -->
    <header class="app-layout__header">
      <AppHeaderMobile v-if="isMobile" />
      <AppHeader v-else />
    </header>

    <!-- Main Content Area -->
    <div class="app-layout__body">
      <!-- Sidebar -->
      <nav class="app-layout__sidebar" v-if="!isMobile">
        <AppSidebar />
      </nav>

      <!-- Main Content -->
      <main class="app-layout__main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
  .app-layout {
    display: flex;
    flex-direction: column;
    height: 100vh;
    width: 100%;
    overflow: hidden;
    margin: 0;
    padding: 0;
  }

  .app-layout__header {
    flex-shrink: 0;
    z-index: 100;
    width: 100%;
  }

  .app-layout__body {
    display: flex;
    flex: 1;
    overflow: hidden;
    width: 100%;
  }

  .app-layout__sidebar {
    width: 250px;
    flex-shrink: 0;
    overflow-y: auto;
    height: 100%;
  }

  .app-layout__main {
    flex: 1;
    overflow-y: auto;
    padding: 2rem;
  }
</style>
