import { describe, it, expect, vi } from 'vitest'
import { mountWithPlugins } from '@/test-utils/vue-test-utils'
import MediaHero from '../MediaHero.vue'

// Mock vue-router since MediaHero uses useRouter for genre navigation
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))
describe('MediaHero', () => {
  const mockGenres = [
    { id: '1', name: 'Action' },
    { id: '2', name: 'Adventure' },
    { id: '3', name: 'Sci-Fi' },
    { id: '4', name: 'Thriller' },
  ]

  const defaultProps = {
    title: 'Test Movie',
    backdropUrl: 'https://example.com/backdrop.jpg',
    year: 2024,
    runtime: 142,
    genres: mockGenres,
    myRating: undefined,
    showRating: false,
  }

  describe('Rendering', () => {
    it('renders title correctly', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: defaultProps,
      })

      const title = wrapper.find('.hero-title')
      expect(title.text()).toBe('Test Movie')
    })

    it('renders backdrop as background image', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: defaultProps,
      })

      const hero = wrapper.find('.media-hero')
      expect(hero.attributes('style')).toContain('background-image')
      expect(hero.attributes('style')).toContain('https://example.com/backdrop.jpg')
    })

    it('renders year when provided', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: defaultProps,
      })

      expect(wrapper.text()).toContain('2024')
    })

    it('does not render year when not provided', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          year: undefined,
        },
      })

      const detailItems = wrapper.findAll('.detail-item')
      expect(detailItems.length).toBe(1) // Only runtime
    })
  })

  describe('Runtime Formatting', () => {
    it('formats runtime in hours and minutes', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          runtime: 142, // 2h 22m
        },
      })

      expect(wrapper.text()).toContain('2h 22m')
    })

    it('formats runtime with only minutes when less than 60', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          runtime: 45,
        },
      })

      expect(wrapper.text()).toContain('45m')
      expect(wrapper.text()).not.toContain('0h')
    })

    it('formats runtime correctly for exactly 1 hour', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          runtime: 60,
        },
      })

      expect(wrapper.text()).toContain('1h 0m')
    })
  })

  describe('Genres', () => {
    it('renders first 3 genres as chips', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: defaultProps,
        global: {
          stubs: {
            Chip: false,
          },
        },
      })

      const chips = wrapper.findAll('.genre-chip')
      expect(chips.length).toBe(3)
    })

    it('displays correct genre names', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: defaultProps,
        global: {
          stubs: {
            Chip: false,
          },
        },
      })

      expect(wrapper.text()).toContain('Action')
      expect(wrapper.text()).toContain('Adventure')
      expect(wrapper.text()).toContain('Sci-Fi')
      expect(wrapper.text()).not.toContain('Thriller') // 4th genre not shown
    })

    it('does not render genres section when empty', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          genres: [],
        },
      })

      const genresDiv = wrapper.find('.genres')
      expect(genresDiv.exists()).toBe(false)
    })
  })

  describe('Mobile Rating Button', () => {
    it('does not show rating button when showRating is false', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          showRating: false,
        },
      })

      const ratingOverlay = wrapper.find('.mobile-rating-overlay')
      expect(ratingOverlay.exists()).toBe(false)
    })

    it('shows rating button when showRating is true', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          showRating: true,
        },
      })

      const ratingOverlay = wrapper.find('.mobile-rating-overlay')
      expect(ratingOverlay.exists()).toBe(true)
    })

    it('displays rating value when myRating is provided', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          showRating: true,
          myRating: 9.5,
        },
      })

      const ratingValue = wrapper.find('.rating-value')
      expect(ratingValue.exists()).toBe(true)
      expect(ratingValue.text()).toBe('9.5')
    })

    it('displays "Rate" text when myRating is not provided', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          showRating: true,
          myRating: undefined,
        },
      })

      const ratingEmpty = wrapper.find('.rating-empty')
      expect(ratingEmpty.exists()).toBe(true)
      expect(ratingEmpty.text()).toBe('Rate')
    })

    it('emits click:rating when rating button is clicked', async () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          showRating: true,
        },
      })

      const ratingOverlay = wrapper.find('.mobile-rating-overlay')
      await ratingOverlay.trigger('click')

      expect(wrapper.emitted('click:rating')).toBeTruthy()
      expect(wrapper.emitted('click:rating')?.length).toBe(1)
    })

    it('formats rating to 1 decimal place', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          showRating: true,
          myRating: 7,
        },
      })

      const ratingValue = wrapper.find('.rating-value')
      expect(ratingValue.text()).toBe('7.0')
    })
  })

  describe('Icon Usage', () => {
    it('uses Icon enum for star icon in mobile rating button', () => {
      const wrapper = mountWithPlugins(MediaHero, {
        props: {
          ...defaultProps,
          showRating: true,
        },
      })

      const starIcon = wrapper.find('.mobile-rating-overlay .star-icon')
      expect(starIcon.classes()).toContain('pi')
      expect(starIcon.classes()).toContain('pi-star')
    })
  })
})
