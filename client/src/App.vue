<script setup lang="ts">
  import { onMounted } from 'vue'
  import { Toaster } from 'vue-sonner'
  import AppLayout from '@/components/layout/AppLayout.vue'

  // Apply dark mode class to HTML element for PrimeVue v4
  onMounted(() => {
    document.documentElement.classList.add('app-dark')
  })
</script>

<template>
  <AppLayout />

  <!-- Toast notifications container -->
  <Toaster
    position="bottom-right"
    :rich-colors="true"
    :close-button="true"
    :duration="4000"
    theme="dark"
  />
</template>
