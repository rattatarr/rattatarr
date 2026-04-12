<script setup lang="ts">
  import Card from 'primevue/card'
  import { useProfileStore } from '@/stores'
  import { Icon } from '@/utils/enums'

  const profileStore = useProfileStore()
</script>

<template>
  <div class="ratings-view">
    <Card v-if="!profileStore.hasSelectedProfile" severity="warn">
      <template #title>
        <i :class="Icon.EXCLAMATION_TRIANGLE"></i>
        No Profile Selected
      </template>
      <template #content>
        <p>Please select a profile from the header to view your ratings.</p>
      </template>
    </Card>

    <Card v-else>
      <template #title>Your Ratings</template>
      <template #content>
        <p>Here you'll see all your movie and series ratings.</p>
        <p class="text-secondary">
          <i :class="Icon.INFO_CIRCLE"></i>
          Ratings list will be implemented next.
        </p>
      </template>
    </Card>
  </div>
</template>

<style scoped>
  .ratings-view {
    padding-bottom: 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  .page-title {
    margin-bottom: 1.5rem;
    color: var(--p-text-color);
  }

  .text-secondary {
    color: var(--p-text-secondary-color);
    margin-top: 1rem;
  }
</style>
