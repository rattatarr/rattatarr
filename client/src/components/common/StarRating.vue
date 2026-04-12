<script setup lang="ts">
  import { ref, computed } from 'vue'

  interface Props {
    modelValue: number
    stars?: number
  }

  interface Emits {
    (e: 'update:modelValue', value: number): void
  }

  const props = withDefaults(defineProps<Props>(), {
    stars: 10,
  })

  const emit = defineEmits<Emits>()

  const hoverValue = ref<number | null>(null)

  const displayValue = computed(() => {
    return hoverValue.value !== null ? hoverValue.value : props.modelValue
  })

  const displayText = computed(() => {
    const value = displayValue.value
    return value > 0 ? value.toFixed(1) : '0.0'
  })

  const isHovering = computed(() => hoverValue.value !== null)

  function onContainerHover(event: MouseEvent) {
    // Find which star container the mouse is over
    const target = event.target as HTMLElement
    const starContainer = target.closest('.star-container') as HTMLElement

    if (!starContainer) {
      return
    }

    const star = Number(starContainer.dataset.star)
    const rect = starContainer.getBoundingClientRect()
    const x = event.clientX - rect.left
    const isLeftHalf = x < rect.width / 2

    // Show half star if hovering left side, full star if right side
    hoverValue.value = isLeftHalf ? star - 0.5 : star
  }

  function onMouseLeave() {
    hoverValue.value = null
  }

  function onStarClick(star: number, event: MouseEvent) {
    const target = event.currentTarget as HTMLElement
    const rect = target.getBoundingClientRect()
    const x = event.clientX - rect.left
    const isLeftHalf = x < rect.width / 2

    // Set half star if clicking left side, full star if right side
    const newValue = isLeftHalf ? star - 0.5 : star
    emit('update:modelValue', newValue)
  }
</script>

<template>
  <div class="star-rating-wrapper">
    <div class="star-rating" @mousemove="onContainerHover" @mouseleave="onMouseLeave">
      <div
        v-for="star in stars"
        :key="star"
        class="star-container"
        :data-star="star"
        @click="onStarClick(star, $event)"
      >
        <i
          class="star-icon pi pi-star-fill"
          :class="{
            filled: star <= Math.floor(displayValue),
            'half-filled': star === Math.ceil(displayValue) && displayValue % 1 !== 0,
            empty: star > Math.ceil(displayValue),
          }"
        />
      </div>
    </div>
    <div class="rating-display" :class="{ hovering: isHovering }">{{ displayText }} / 10.0</div>
  </div>
</template>

<style scoped>
  .star-rating-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }

  .star-rating {
    display: flex;
    gap: 0.5rem;
    justify-content: center;
  }

  .rating-display {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--p-text-color-secondary);
    transition: all 0.2s ease;
    min-width: 100px;
    text-align: center;
  }

  .rating-display.hovering {
    color: var(--primary-color);
    transform: scale(1.1);
  }

  .star-container {
    cursor: pointer;
    position: relative;
    user-select: none;
  }

  .star-icon {
    font-size: 2.5rem;
    transition: color 0.15s ease;
  }

  .star-icon.empty {
    color: #e0e0e0; /* Light gray for unfilled */
  }

  .star-icon.filled {
    color: #f5c518; /* IMDb yellow for filled */
  }

  .star-icon.half-filled {
    background: linear-gradient(
      to right,
      #f5c518 0%,
      #f5c518 40%,
      #f9d75e 50%,
      #fce9a0 55%,
      #ffffff 65%,
      #ffffff 100%
    );
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  /* Hover effect - subtle glow instead of color change */
  .star-container:hover .star-icon {
    filter: drop-shadow(0 0 3px rgba(245, 197, 24, 0.5));
  }
</style>
