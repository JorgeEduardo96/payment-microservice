import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatCard from '../StatCard.vue'

describe('StatCard', () => {
  it('renders the icon, label and value', () => {
    const wrapper = mount(StatCard, {
      props: { icon: 'mdi-account-group', iconColor: 'primary', label: 'Clients Loaded', value: 5, to: '/clients' },
    })

    expect(wrapper.find('.mdi-account-group').exists()).toBe(true)
    expect(wrapper.text()).toContain('Clients Loaded')
    expect(wrapper.text()).toContain('5')
  })

  it('does not render the value element when value is 0', () => {
    const wrapper = mount(StatCard, {
      props: { icon: 'mdi-cart', iconColor: 'secondary', label: 'Orders', value: 0, to: '/orders' },
    })

    expect(wrapper.find('.text-h4').exists()).toBe(false)
    expect(wrapper.text()).toContain('Orders')
  })

  it('applies the iconColor to the value text', () => {
    const wrapper = mount(StatCard, {
      props: { icon: 'mdi-check-circle', iconColor: 'success', label: 'Paid', value: 3, to: '/orders' },
    })

    expect(wrapper.find('.text-success').exists()).toBe(true)
    expect(wrapper.text()).toContain('3')
  })
})
