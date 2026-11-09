import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import App from '@/App.vue'
import { useUserStore } from '@/stores/user-store'
import { createTestRouter, routerStubs } from './helpers/router'

describe('App', () => {
  beforeEach(() => {
    const pinia = createPinia()
    setActivePinia(pinia)
    localStorage.clear()
  })

  async function mountApp(stubs: Record<string, unknown> = routerStubs) {
    const pinia = createPinia()
    setActivePinia(pinia)
    const router = createTestRouter()
    await router.isReady()

    const wrapper = mount(App, {
      global: {
        plugins: [pinia, router],
        stubs,
      },
    })

    return { wrapper, router, pinia }
  }

  it('renders properly', async () => {
    const { wrapper } = await mountApp({
      ...routerStubs,
      GlobalToast: true,
    })
    expect(wrapper.text()).toContain('ABET Assessment App')
  })

  it('renders layout elements properly', async () => {
    const { wrapper } = await mountApp({
      NavBar: true,
      RouterView: true,
      GlobalToast: true,
    })
    expect(wrapper.find('#app').exists()).toBe(true)
    expect(wrapper.find('#fixed-top').exists()).toBe(true)
    expect(wrapper.find('main').exists()).toBe(true)
    expect(wrapper.find('footer').exists()).toBe(true)
  })

  it('initializes with default (logged out) user state', async () => {
    const { wrapper } = await mountApp({
      NavBar: true,
      RouterView: true,
      GlobalToast: true,
    })

    const navBar = wrapper.findComponent({ name: 'NavBar' })

    expect(navBar.props('loggedIn')).toBe(false)
    expect(navBar.props('username')).toBe('')
  })

  it('handles logout correctly', async () => {
    const { wrapper } = await mountApp({
      NavBar: true,
      RouterView: true,
      GlobalToast: true,
    })

    const userStore = useUserStore()
    const navBar = wrapper.findComponent({ name: 'NavBar' })

    expect(navBar.props('loggedIn')).toBe(false)

    userStore.$patch({
      authToken: 'fake-token-123',
      user: {
        id: 1,
        email: 'test@user.com',
        firstName: 'Test',
        lastName: 'User',
        role: 'INSTRUCTOR',
      },
    })

    await wrapper.vm.$nextTick()

    expect(navBar.props('loggedIn')).toBe(true)
    expect(navBar.props('username')).toBe('Test User')

    await navBar.vm.$emit('logout')
    await wrapper.vm.$nextTick()

    expect(navBar.props('loggedIn')).toBe(false)
    expect(navBar.props('username')).toBe('')
  })
})
