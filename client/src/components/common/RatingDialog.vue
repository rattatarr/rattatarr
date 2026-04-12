<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import Dialog from 'primevue/dialog'
  import Button from 'primevue/button'
  import { Icon, ButtonSeverity } from '@/utils/enums.ts'
  import StarRating from '@/components/common/StarRating.vue'

  interface Props {
    visible: boolean
    title?: string
    myRating?: number
    backdropUrl?: string
    isPending?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    isPending: false,
  })

  const emit = defineEmits<{
    'update:visible': [value: boolean]
    submit: [rating: number]
  }>()

  const internalRating = ref(0)

  // Computed header with "Rate: " prefix
  const header = computed(() => `Rate: ${props.title || 'Media'}`)

  // Responsive dialog style
  const dialogStyle = computed(() => {
    const isMobile = window.innerWidth <= 768
    if (isMobile) {
      return {
        width: '100vw',
        maxWidth: '100vw',
        margin: '0',
      }
    }
    return {
      width: '800px',
      maxWidth: '90vw',
    }
  })

  // Watch for dialog open to set initial rating
  watch(
    () => props.visible,
    (newVisible) => {
      if (newVisible) {
        internalRating.value = props.myRating ?? 0
      }
    },
  )

  const handleVisibilityChange = (value: boolean) => {
    emit('update:visible', value)
    if (!value) {
      // Reset rating when dialog closes
      internalRating.value = 0
    }
  }

  const handleSubmit = () => {
    if (internalRating.value >= 0.5 && internalRating.value <= 10) {
      emit('submit', internalRating.value)
    }
  }

  const handleCancel = () => {
    emit('update:visible', false)
  }
</script>

<template>
  <Dialog
    :visible="visible"
    modal
    :header="header"
    :style="dialogStyle"
    :dismissableMask="true"
    class="rating-dialog"
    @update:visible="handleVisibilityChange"
  >
    <!-- Backdrop in Dialog -->
    <div
      v-if="backdropUrl"
      class="dialog-backdrop"
      :style="{ backgroundImage: `url(${backdropUrl})` }"
    />

    <div class="rating-dialog-content">
      <StarRating v-model="internalRating" :stars="10" />
    </div>

    <template #footer>
      <div class="rating-dialog-footer">
        <Button
          :label="myRating ? 'Update Rating' : 'Submit Rating'"
          :icon="Icon.CHECK"
          :severity="ButtonSeverity.PRIMARY"
          :loading="isPending"
          @click="handleSubmit"
        />
        <Button
          label="Cancel"
          :icon="Icon.TIMES"
          :severity="ButtonSeverity.SECONDARY"
          text
          @click="handleCancel"
        />
      </div>
    </template>
  </Dialog>
</template>

<style scoped>
  .rating-dialog :deep(.p-dialog-content) {
    position: relative;
    overflow: hidden;
  }

  .dialog-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    filter: blur(2px);
    opacity: 0.15;
    z-index: 0;
    pointer-events: none;
  }

  .rating-dialog-content {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 20px 0;
    z-index: 1;
    width: 100%;
  }

  .rating-dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }

  /* Mobile responsive */
  @media (max-width: 768px) {
    .rating-dialog :deep(.p-dialog-header) {
      padding: 1rem;
      font-size: 1.125rem;
    }

    .rating-dialog :deep(.p-dialog-content) {
      padding: 0.75rem;
    }

    .rating-dialog-content {
      padding: 0.5rem;
      gap: 1rem;
      max-width: 100%;
      overflow-x: hidden;
    }

    /* Make stars smaller on mobile */
    .rating-dialog-content :deep(.star-rating) {
      transform: scale(0.6);
      transform-origin: center;
    }

    .rating-dialog-content :deep(.p-rating) {
      gap: 0.15rem;
    }

    .rating-dialog-content :deep(.p-rating-icon) {
      font-size: 1.25rem;
    }

    /* Buttons: Update left, Cancel right */
    .rating-dialog-footer {
      flex-direction: row;
      gap: 0.5rem;
      justify-content: space-between;
    }

    .rating-dialog-footer :deep(.p-button) {
      flex: 1;
      justify-content: center;
      font-size: 0.875rem;
      padding: 0.625rem 1rem;
    }

    .rating-dialog-footer :deep(.p-button .p-button-label) {
      white-space: nowrap;
    }
  }
</style>
