<script setup lang="ts">
  import Button from 'primevue/button'
  import { Icon } from '@/utils/enums'

  interface Props {
    show?: boolean
    onScroll?: () => void
  }

  const props = withDefaults(defineProps<Props>(), {
    show: false,
    onScroll: undefined,
  })

  function scrollToTop() {
    if (props.onScroll) {
      // Use custom scroll handler if provided
      props.onScroll()
    } else {
      // Fallback: Try multiple methods to ensure scrolling works
      window.scrollTo({ top: 0, behavior: 'smooth' })
      document.documentElement.scrollTo({ top: 0, behavior: 'smooth' })
      document.body.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }
</script>

<template>
  <Transition name="fade-scale">
    <Button
      v-if="show"
      :icon="Icon.CHEVRON_UP"
      severity="primary"
      rounded
      raised
      class="scroll-to-top"
      aria-label="Scroll to top"
      @click="scrollToTop"
    />
  </Transition>
</template>

<style scoped>
  .scroll-to-top {
    position: fixed !important;
    bottom: 2rem !important;
    right: 2rem !important;
    z-index: 99999 !important;
    width: 3.5rem !important;
    height: 3.5rem !important;
  }

  .scroll-to-top:hover {
    transform: translateY(-4px) !important;
  }

  .scroll-to-top:active {
    transform: translateY(-2px) !important;
  }

  /* Fade and scale animation */
  .fade-scale-enter-active,
  .fade-scale-leave-active {
    transition: all 0.3s ease;
  }

  .fade-scale-enter-from,
  .fade-scale-leave-to {
    opacity: 0;
    transform: scale(0.8) translateY(20px);
  }

  .fade-scale-enter-to,
  .fade-scale-leave-from {
    opacity: 1;
    transform: scale(1) translateY(0);
  }

  /* Responsive */
  @media (max-width: 768px) {
    .scroll-to-top {
      bottom: 1.5rem !important;
      right: 1.5rem !important;
      width: 3rem !important;
      height: 3rem !important;
    }
  }
</style>
