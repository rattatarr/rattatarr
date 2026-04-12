import { describe, it, expect } from 'vitest'
import { ref, defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { provideLogsContext, useLogsContext } from '../useLogsContext'
import type { LogsFilters } from '@/api/logs'

describe('useLogsContext', () => {
  describe('provideLogsContext / useLogsContext', () => {
    it('should provide and inject context successfully', () => {
      const filters = ref<LogsFilters>({
        level: 'INFO',
        logger: 'APP',
      })
      const autoRefresh = ref(false)

      // Provider component
      const ProviderComponent = defineComponent({
        setup() {
          provideLogsContext({
            filters,
            autoRefresh,
          })
          return {}
        },
        template: '<div><slot /></div>',
      })

      // Consumer component
      const ConsumerComponent = defineComponent({
        setup() {
          const context = useLogsContext()
          return { level: context.filters.value.level }
        },
        template: '<div>{{ level }}</div>',
      })

      const wrapper = mount(ProviderComponent, {
        slots: {
          default: ConsumerComponent,
        },
      })

      expect(wrapper.text()).toBe('INFO')
    })

    it('should share reactive state between provider and consumer', async () => {
      const filters = ref<LogsFilters>({
        level: 'INFO',
        logger: 'APP',
      })
      const autoRefresh = ref(false)

      // Provider component
      const ProviderComponent = defineComponent({
        setup() {
          provideLogsContext({
            filters,
            autoRefresh,
          })
          return {}
        },
        template: '<div><slot /></div>',
      })

      // Consumer component
      const ConsumerComponent = defineComponent({
        setup() {
          const context = useLogsContext()
          return {
            filters: context.filters,
            autoRefresh: context.autoRefresh,
          }
        },
        template: '<div>{{ filters.level }}-{{ autoRefresh }}</div>',
      })

      const wrapper = mount(ProviderComponent, {
        slots: {
          default: ConsumerComponent,
        },
      })

      expect(wrapper.text()).toBe('INFO-false')

      // Update filters
      filters.value = { level: 'ERROR', logger: 'DB' }
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toBe('ERROR-false')

      // Update autoRefresh
      autoRefresh.value = true
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toBe('ERROR-true')
    })

    it('should allow consumer to modify context refs', async () => {
      const filters = ref<LogsFilters>({
        level: 'INFO',
        logger: 'APP',
      })
      const autoRefresh = ref(false)

      // Provider component
      const ProviderComponent = defineComponent({
        setup() {
          provideLogsContext({
            filters,
            autoRefresh,
          })
          return { filters, autoRefresh }
        },
        template: '<div>{{ filters.level }}-{{ autoRefresh }}<slot /></div>',
      })

      // Consumer component that modifies context
      const ConsumerComponent = defineComponent({
        setup() {
          const context = useLogsContext()

          const updateFilters = () => {
            context.filters.value = { level: 'WARN', logger: 'API' }
          }

          const toggleAutoRefresh = () => {
            context.autoRefresh.value = !context.autoRefresh.value
          }

          return { updateFilters, toggleAutoRefresh }
        },
        template: `
          <div>
            <button @click="updateFilters" id="update-filters">Update</button>
            <button @click="toggleAutoRefresh" id="toggle-refresh">Toggle</button>
          </div>
        `,
      })

      const wrapper = mount(ProviderComponent, {
        slots: {
          default: ConsumerComponent,
        },
      })

      expect(wrapper.text()).toContain('INFO-false')

      // Consumer modifies filters
      await wrapper.find('#update-filters').trigger('click')
      expect(wrapper.text()).toContain('WARN-false')

      // Consumer toggles autoRefresh
      await wrapper.find('#toggle-refresh').trigger('click')
      expect(wrapper.text()).toContain('WARN-true')
    })
  })

  describe('Error Handling', () => {
    it('should throw error when used without provider', () => {
      const ConsumerComponent = defineComponent({
        setup() {
          useLogsContext() // This should throw
          return {}
        },
        template: '<div>Consumer</div>',
      })

      // Mount without provider - setup will throw before render
      expect(() => {
        mount(ConsumerComponent)
      }).toThrow('useLogsContext must be used within a component that provides LogsContext')
    })

    it('should throw with correct error message', () => {
      const ConsumerComponent = defineComponent({
        setup() {
          try {
            useLogsContext()
          } catch (error) {
            expect(error).toBeInstanceOf(Error)
            expect((error as Error).message).toBe(
              'useLogsContext must be used within a component that provides LogsContext',
            )
            throw error // Re-throw to prevent component mounting
          }
          return {}
        },
        template: '<div>Consumer</div>',
      })

      expect(() => mount(ConsumerComponent)).toThrow()
    })
  })

  describe('Multiple Consumers', () => {
    it('should allow multiple consumers to access same context', () => {
      const filters = ref<LogsFilters>({
        level: 'DEBUG',
        logger: 'SYSTEM',
      })
      const autoRefresh = ref(true)

      // Provider component
      const ProviderComponent = defineComponent({
        setup() {
          provideLogsContext({
            filters,
            autoRefresh,
          })
          return {}
        },
        template: '<div><slot /></div>',
      })

      // Consumer 1
      const Consumer1 = defineComponent({
        setup() {
          const context = useLogsContext()
          return { level: context.filters.value.level }
        },
        template: '<span>{{ level }}</span>',
      })

      // Consumer 2
      const Consumer2 = defineComponent({
        setup() {
          const context = useLogsContext()
          return { logger: context.filters.value.logger }
        },
        template: '<span>{{ logger }}</span>',
      })

      // Consumer 3
      const Consumer3 = defineComponent({
        setup() {
          const context = useLogsContext()
          return { autoRefresh: context.autoRefresh.value }
        },
        template: '<span>{{ autoRefresh }}</span>',
      })

      const WrapperComponent = defineComponent({
        components: { Consumer1, Consumer2, Consumer3 },
        template: '<div><Consumer1 /><Consumer2 /><Consumer3 /></div>',
      })

      const wrapper = mount(ProviderComponent, {
        slots: {
          default: WrapperComponent,
        },
      })

      const spans = wrapper.findAll('span')
      expect(spans[0]?.text()).toBe('DEBUG')
      expect(spans[1]?.text()).toBe('SYSTEM')
      expect(spans[2]?.text()).toBe('true')
    })
  })

  describe('Nested Providers', () => {
    it('should use closest parent provider', () => {
      const outerFilters = ref<LogsFilters>({
        level: 'INFO',
        logger: 'OUTER',
      })
      const outerAutoRefresh = ref(false)

      const innerFilters = ref<LogsFilters>({
        level: 'ERROR',
        logger: 'INNER',
      })
      const innerAutoRefresh = ref(true)

      // Outer provider
      const OuterProvider = defineComponent({
        setup() {
          provideLogsContext({
            filters: outerFilters,
            autoRefresh: outerAutoRefresh,
          })
          return {}
        },
        template: '<div>Outer: <slot /></div>',
      })

      // Inner provider
      const InnerProvider = defineComponent({
        setup() {
          provideLogsContext({
            filters: innerFilters,
            autoRefresh: innerAutoRefresh,
          })
          return {}
        },
        template: '<div>Inner: <slot /></div>',
      })

      // Consumer inside inner provider
      const InnerConsumer = defineComponent({
        setup() {
          const context = useLogsContext()
          return { logger: context.filters.value.logger }
        },
        template: '<span>{{ logger }}</span>',
      })

      // Consumer inside outer provider (but outside inner)
      const OuterConsumer = defineComponent({
        components: { InnerProvider, InnerConsumer },
        setup() {
          const context = useLogsContext()
          return { logger: context.filters.value.logger }
        },
        template: `
          <div>
            <span id="outer">{{ logger }}</span>
            <InnerProvider>
              <InnerConsumer />
            </InnerProvider>
          </div>
        `,
      })

      const wrapper = mount(OuterProvider, {
        slots: {
          default: OuterConsumer,
        },
      })

      // Outer consumer should use outer context
      expect(wrapper.find('#outer').text()).toBe('OUTER')

      // Inner consumer should use inner context (closest parent)
      const allSpans = wrapper.findAll('span')
      expect(allSpans[1]?.text()).toBe('INNER')
    })
  })
})
