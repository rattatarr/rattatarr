<script setup lang="ts">
  import Dialog from 'primevue/dialog'
  import Button from 'primevue/button'
  import Avatar from 'primevue/avatar'
  import { useRouter } from 'vue-router'
  import { Icon, getInitials } from '@/utils'
  import type { Person } from '@/types'

  interface Props {
    visible: boolean
    person: Person | null
  }

  const props = defineProps<Props>()

  const emit = defineEmits<{
    'update:visible': [value: boolean]
  }>()

  const router = useRouter()

  function close() {
    emit('update:visible', false)
  }

  function navigateTo(routeName: 'movies' | 'series') {
    close()
    if (!props.person?.id) return
    router.push({
      name: routeName,
      query: { personId: props.person.id },
    })
  }
</script>

<template>
  <Dialog
    :visible="visible"
    modal
    header="Filter by Person"
    :style="{ width: '360px', maxWidth: '90vw' }"
    :dismissable-mask="true"
    class="person-filter-dialog"
    @update:visible="close"
  >
    <div class="dialog-content">
      <div class="person-info">
        <div class="person-avatar">
          <img
            v-if="person?.profilePathUrl"
            :src="person.profilePathUrl"
            :alt="person?.name ?? ''"
            class="avatar-img"
          />
          <Avatar
            v-else
            :label="getInitials(person?.name)"
            shape="circle"
            size="xlarge"
            class="avatar-fallback"
          />
        </div>
        <div class="person-name">{{ person?.name }}</div>
      </div>
      <p class="dialog-question">See titles with this person in:</p>
      <div class="action-buttons">
        <Button label="Movies" :icon="Icon.FILM" class="action-btn" @click="navigateTo('movies')" />
        <Button
          label="Series"
          :icon="Icon.TH_LARGE"
          class="action-btn"
          @click="navigateTo('series')"
        />
      </div>
    </div>
  </Dialog>
</template>

<style scoped>
  .dialog-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.25rem;
    padding: 0.5rem 0;
  }

  .person-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;
  }

  .person-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    overflow: hidden;
    flex-shrink: 0;
  }

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .avatar-fallback {
    width: 100%;
    height: 100%;
  }

  .person-name {
    font-size: 1.125rem;
    font-weight: 600;
    color: var(--p-text-color);
    text-align: center;
  }

  .dialog-question {
    font-size: 0.9rem;
    color: var(--p-text-color-secondary);
    margin: 0;
    text-align: center;
  }

  .action-buttons {
    display: flex;
    gap: 0.75rem;
    width: 100%;
    justify-content: center;
  }

  .action-btn {
    flex: 1;
    max-width: 140px;
  }
</style>
