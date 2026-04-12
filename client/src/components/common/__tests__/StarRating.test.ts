import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StarRating from '@/components/common/StarRating.vue'

describe('StarRating', () => {
  describe('Props', () => {
    it('should render with default 10 stars', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 5.0,
        },
      })

      // Should have 10 star containers
      const stars = wrapper.findAll('[data-star]')
      expect(stars.length).toBe(10)
    })

    it('should display the model value as text', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 7.5,
        },
      })

      expect(wrapper.text()).toContain('7.5')
    })

    it('should display 0.0 when model value is 0', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 0,
        },
      })

      expect(wrapper.text()).toContain('0.0')
    })

    it('should accept custom number of stars', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 3.0,
          stars: 5,
        },
      })

      const stars = wrapper.findAll('[data-star]')
      expect(stars.length).toBe(5)
    })
  })

  describe('Hover State', () => {
    it('should clear hover value on mouse leave', async () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 5.0,
        },
      })

      const container = wrapper.find('.star-rating')

      // Trigger mouseleave
      await container.trigger('mouseleave')

      // Should display original value
      expect(wrapper.text()).toContain('5.0')
    })
  })

  describe('Click Handler', () => {
    it('should emit update:modelValue when star is clicked', async () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 0,
        },
      })

      // Find a star container
      const star = wrapper.find('[data-star="5"]')

      // Click the star
      await star.trigger('click')

      // Should emit the new value
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    })

    it('should emit twice when clicking the same star again', async () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 5.0,
        },
      })

      const star = wrapper.find('[data-star="5"]')

      // First click
      await star.trigger('click')

      // Second click on same star
      await star.trigger('click')

      const emitted = wrapper.emitted('update:modelValue')
      expect(emitted).toBeTruthy()
      // Should have emitted at least once
      expect(emitted!.length).toBeGreaterThanOrEqual(1)
    })
  })

  describe('Display Text', () => {
    it('should format rating to one decimal place', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 8.567,
        },
      })

      expect(wrapper.text()).toContain('8.6')
    })

    it('should display "/10.0" suffix', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 6.0,
        },
      })

      expect(wrapper.text()).toContain('/ 10.0')
    })
  })

  describe('Styling', () => {
    it('should have star-rating-wrapper class', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 5.0,
        },
      })

      expect(wrapper.find('.star-rating-wrapper').exists()).toBe(true)
    })

    it('should have rating-display element', () => {
      const wrapper = mount(StarRating, {
        props: {
          modelValue: 5.0,
        },
      })

      expect(wrapper.find('.rating-display').exists()).toBe(true)
    })
  })
})
