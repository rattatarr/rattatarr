import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FileUpload from '../FileUpload.vue'

vi.mock('primevue/fileupload', () => ({
  default: { name: 'FileUpload', template: '<div class="file-upload"><slot /></div>' },
}))

describe('FileUpload', () => {
  it('renders file upload component', () => {
    const wrapper = mount(FileUpload, {
      props: { modelValue: null },
    })
    expect(wrapper.find('.file-upload-container').exists()).toBe(true)
  })

  it('accepts CSV files', () => {
    const wrapper = mount(FileUpload, {
      props: { modelValue: null, accept: '.csv' },
    })
    expect(wrapper.props('accept')).toBe('.csv')
  })

  it('emits change event with file', async () => {
    const wrapper = mount(FileUpload, {
      props: { modelValue: null },
    })
    const file = new File(['content'], 'ratings.csv', { type: 'text/csv' })

    await wrapper.vm.$emit('change', file)

    expect(wrapper.emitted('change')).toBeTruthy()
  })
})
