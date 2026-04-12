<script setup lang="ts">
  import { ref, computed } from 'vue'
  import Avatar from 'primevue/avatar'
  import { getInitials } from '@/utils'

  interface Props {
    name: string
    profileUrl?: string
    character?: string
  }

  const props = defineProps<Props>()
  const imageError = ref(false)

  const initials = computed(() => getInitials(props.name))
</script>

<template>
  <div class="cast-card">
    <div class="avatar-wrapper">
      <img
        v-if="profileUrl && !imageError"
        :src="profileUrl"
        :alt="name"
        class="cast-avatar-image"
        @error="imageError = true"
      />
      <Avatar v-else :label="initials" class="cast-avatar-fallback" shape="circle" size="xlarge" />
    </div>
    <div class="cast-info">
      <div class="cast-name">{{ name }}</div>
      <div v-if="character" class="cast-character">as {{ character }}</div>
    </div>
  </div>
</template>

<style scoped>
  .cast-card {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0.5rem;
    border-radius: var(--border-radius);
    transition: background-color 0.2s;
  }

  .cast-card:hover {
    background-color: var(--surface-hover);
  }

  .avatar-wrapper {
    flex-shrink: 0;
    width: 60px;
    height: 60px;
  }

  .cast-avatar-image {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
  }

  .cast-avatar-fallback {
    width: 100%;
    height: 100%;
  }

  .cast-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    min-width: 0;
  }

  .cast-name {
    font-weight: 600;
    color: var(--p-text-color);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .cast-character {
    font-size: 0.875rem;
    color: var(--p-text-color-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
</style>
