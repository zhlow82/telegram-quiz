import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { nextTick } from 'vue'

import AppDialog from '@/components/AppDialog.vue'

function mountDialog() {
  return mount(AppDialog, {
    props: {
      visible: false,
      type: 'confirm',
      title: 'Delete quiz',
      message: 'This cannot be undone.',
      confirmLabel: 'Delete'
    },
    global: {
      stubs: { teleport: true }
    }
  })
}

async function show(wrapper: ReturnType<typeof mountDialog>) {
  await wrapper.setProps({ visible: true })
  await nextTick()
}

describe('AppDialog', () => {
  it('does not cancel when the backdrop is clicked', async () => {
    const wrapper = mountDialog()
    await show(wrapper)

    await wrapper.find('.absolute.inset-0').trigger('click')

    expect(wrapper.emitted('cancel')).toBeUndefined()
  })

  it('emits cancel from the close button', async () => {
    const wrapper = mountDialog()
    await show(wrapper)

    await wrapper.find('button[aria-label="Close"]').trigger('click')

    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('emits cancel from the Cancel button', async () => {
    const wrapper = mountDialog()
    await show(wrapper)

    const cancel = wrapper.findAll('button').find(button => button.text() === 'Cancel')
    await cancel!.trigger('click')

    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('emits cancel on Escape', async () => {
    const wrapper = mountDialog()
    await show(wrapper)

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await nextTick()

    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('emits confirm from the confirm button', async () => {
    const wrapper = mountDialog()
    await show(wrapper)

    const confirm = wrapper.findAll('button').find(button => button.text() === 'Delete')
    await confirm!.trigger('click')

    expect(wrapper.emitted('confirm')).toHaveLength(1)
  })
})
