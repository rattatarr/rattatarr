import { describe, it, expect, vi, beforeEach } from 'vitest'
import { defineComponent, h, ref, nextTick } from 'vue'
import { mount } from '@vue/test-utils'
import { provideSettingsForm, useSettingsForm } from '@/composables'

// Mock dependencies
const mockSettingsData = ref<any>(null)
const mockIsLoadingQuery = ref(false)
const mockMutateAsync = vi.fn()
const mockIsPending = ref(false)

vi.mock('@/queries/useSettings', () => ({
  useSettings: vi.fn(() => ({
    data: mockSettingsData,
    isLoading: mockIsLoadingQuery,
  })),
  useUpdateSettings: vi.fn(() => ({
    mutateAsync: mockMutateAsync,
    isPending: mockIsPending,
  })),
}))

const mockToastSuccess = vi.fn()
const mockToastError = vi.fn()
const mockToastInfo = vi.fn()
const mockToastLoading = vi.fn(() => 'loading-toast-id')

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    success: mockToastSuccess,
    error: mockToastError,
    info: mockToastInfo,
    loading: mockToastLoading,
  }),
}))

describe('useSettingsForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockSettingsData.value = null
    mockIsLoadingQuery.value = false
    mockIsPending.value = false
    mockMutateAsync.mockResolvedValue({})
  })

  it('throws error when used without provider', () => {
    const TestComponent = defineComponent({
      setup() {
        useSettingsForm()
        return () => h('div')
      },
    })

    expect(() => mount(TestComponent)).toThrow(
      'useSettingsForm must be used within a component wrapped by provideSettingsForm',
    )
  })

  it('initializes with empty settings when no data', () => {
    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        expect(context.settings.value).toEqual({})
        expect(context.hasChanges.value).toBe(false)

        return () => h('div')
      },
    })

    mount(TestComponent)
  })

  it('loads settings from backend data', async () => {
    mockSettingsData.value = {
      settings: [
        { key: 'app.name', value: 'MyApp' },
        { key: 'app.version', value: '1.0.0' },
      ],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [h('div', { id: 'settings' }, JSON.stringify(context.settings.value))])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    expect(wrapper.text()).toContain('app.name')
    expect(wrapper.text()).toContain('MyApp')
  })

  it('updates local setting and marks as changed', async () => {
    mockSettingsData.value = {
      settings: [{ key: 'app.name', value: 'MyApp' }],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [
            h(
              'button',
              {
                onClick: () => context.updateSetting('app.name', 'NewName'),
              },
              'Update',
            ),
            h('div', { id: 'hasChanges' }, String(context.hasChanges.value)),
            h('div', { id: 'value' }, context.getSetting('app.name')),
          ])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    expect(wrapper.find('#hasChanges').text()).toBe('false')

    await wrapper.find('button').trigger('click')

    expect(wrapper.find('#hasChanges').text()).toBe('true')
    expect(wrapper.find('#value').text()).toBe('NewName')
  })

  it('getSetting returns empty string for non-existent key', () => {
    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        const value = context.getSetting('non.existent.key')
        expect(value).toBe('')

        return () => h('div')
      },
    })

    mount(TestComponent)
  })

  it('saveSettings shows info toast when no changes', async () => {
    mockSettingsData.value = {
      settings: [{ key: 'app.name', value: 'MyApp' }],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () => h('button', { onClick: () => context.saveSettings() }, 'Save')
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    await wrapper.find('button').trigger('click')

    expect(mockToastInfo).toHaveBeenCalledWith('No changes to save')
    expect(mockMutateAsync).not.toHaveBeenCalled()
  })

  it('saveSettings sends only changed settings to backend', async () => {
    mockSettingsData.value = {
      settings: [
        { key: 'app.name', value: 'MyApp' },
        { key: 'app.version', value: '1.0.0' },
        { key: 'app.debug', value: 'false' },
      ],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [
            h(
              'button',
              {
                id: 'updateName',
                onClick: () => context.updateSetting('app.name', 'NewName'),
              },
              'Update Name',
            ),
            h(
              'button',
              {
                id: 'updateDebug',
                onClick: () => context.updateSetting('app.debug', 'true'),
              },
              'Update Debug',
            ),
            h('button', { id: 'save', onClick: () => context.saveSettings() }, 'Save'),
          ])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    // Change two settings
    await wrapper.find('#updateName').trigger('click')
    await wrapper.find('#updateDebug').trigger('click')

    // Save
    await wrapper.find('#save').trigger('click')
    await nextTick()

    expect(mockMutateAsync).toHaveBeenCalledWith({
      settings: [
        { key: 'app.name', value: 'NewName' },
        { key: 'app.debug', value: 'true' },
      ],
    })
  })

  it('saveSettings shows success toast on successful save', async () => {
    mockSettingsData.value = {
      settings: [{ key: 'app.name', value: 'MyApp' }],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [
            h(
              'button',
              {
                id: 'update',
                onClick: () => context.updateSetting('app.name', 'NewName'),
              },
              'Update',
            ),
            h('button', { id: 'save', onClick: () => context.saveSettings() }, 'Save'),
          ])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    await wrapper.find('#update').trigger('click')
    await wrapper.find('#save').trigger('click')
    await nextTick()

    expect(mockToastLoading).toHaveBeenCalledWith('Saving settings...', 'Updating configuration')
    expect(mockToastSuccess).toHaveBeenCalledWith(
      'Settings saved',
      expect.objectContaining({
        description: 'Updated 1 setting',
        dismissLoadingToast: 'loading-toast-id',
      }),
    )
  })

  it('saveSettings shows error toast on failure', async () => {
    const mockError = new Error('Network error')
    mockMutateAsync.mockRejectedValue(mockError)

    mockSettingsData.value = {
      settings: [{ key: 'app.name', value: 'MyApp' }],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [
            h(
              'button',
              {
                id: 'update',
                onClick: () => context.updateSetting('app.name', 'NewName'),
              },
              'Update',
            ),
            h('button', { id: 'save', onClick: () => context.saveSettings() }, 'Save'),
          ])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    await wrapper.find('#update').trigger('click')
    await wrapper.find('#save').trigger('click')
    await nextTick()

    expect(mockToastError).toHaveBeenCalledWith(mockError, {
      fallbackMessage: 'Failed to save settings',
      dismissLoadingToast: 'loading-toast-id',
    })
  })

  it('resetSettings discards changes and shows toast', async () => {
    mockSettingsData.value = {
      settings: [{ key: 'app.name', value: 'MyApp' }],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [
            h(
              'button',
              {
                id: 'update',
                onClick: () => context.updateSetting('app.name', 'NewName'),
              },
              'Update',
            ),
            h('button', { id: 'reset', onClick: () => context.resetSettings() }, 'Reset'),
            h('div', { id: 'value' }, context.getSetting('app.name')),
            h('div', { id: 'hasChanges' }, String(context.hasChanges.value)),
          ])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    // Update setting
    await wrapper.find('#update').trigger('click')
    expect(wrapper.find('#value').text()).toBe('NewName')
    expect(wrapper.find('#hasChanges').text()).toBe('true')

    // Reset
    await wrapper.find('#reset').trigger('click')
    await nextTick()

    expect(wrapper.find('#value').text()).toBe('MyApp')
    expect(wrapper.find('#hasChanges').text()).toBe('false')
    expect(mockToastInfo).toHaveBeenCalledWith('Changes discarded')
  })

  it('exposes loading state from query', () => {
    mockIsLoadingQuery.value = true

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        expect(context.isLoading.value).toBe(true)

        return () => h('div')
      },
    })

    mount(TestComponent)
  })

  it('exposes saving state from mutation', () => {
    mockIsPending.value = true

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        expect(context.isSaving.value).toBe(true)

        return () => h('div')
      },
    })

    mount(TestComponent)
  })

  it('savedSettings computed returns original values', async () => {
    mockSettingsData.value = {
      settings: [
        { key: 'app.name', value: 'MyApp' },
        { key: 'app.version', value: '1.0.0' },
      ],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [
            h('div', { id: 'saved' }, JSON.stringify(context.savedSettings.value)),
            h(
              'button',
              {
                onClick: () => context.updateSetting('app.name', 'NewName'),
              },
              'Update',
            ),
          ])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    const savedBefore = wrapper.find('#saved').text()

    // Update setting
    await wrapper.find('button').trigger('click')

    // savedSettings should still be the same (original values)
    expect(wrapper.find('#saved').text()).toBe(savedBefore)
    expect(savedBefore).toContain('MyApp')
  })

  it('clears hasChanges flag after successful save', async () => {
    mockSettingsData.value = {
      settings: [{ key: 'app.name', value: 'MyApp' }],
    }

    const TestComponent = defineComponent({
      setup() {
        const context = provideSettingsForm()

        return () =>
          h('div', [
            h(
              'button',
              {
                id: 'update',
                onClick: () => context.updateSetting('app.name', 'NewName'),
              },
              'Update',
            ),
            h('button', { id: 'save', onClick: () => context.saveSettings() }, 'Save'),
            h('div', { id: 'hasChanges' }, String(context.hasChanges.value)),
          ])
      },
    })

    const wrapper = mount(TestComponent)
    await nextTick()

    await wrapper.find('#update').trigger('click')
    expect(wrapper.find('#hasChanges').text()).toBe('true')

    await wrapper.find('#save').trigger('click')
    await nextTick()

    expect(wrapper.find('#hasChanges').text()).toBe('false')
  })
})
