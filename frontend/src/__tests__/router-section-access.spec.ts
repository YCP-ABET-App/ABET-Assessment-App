import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import { useUserStore } from '@/stores/user-store'
import { authGuard } from '@/router/guards'

vi.mock('@/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
  },
}))

const StubPage = { template: '<div />' }

function createSectionAccessRouter() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/dashboard',
        name: 'Home',
        component: StubPage,
        meta: { requiresAuth: true },
      },
      {
        path: '/section/:section_id',
        name: 'Section',
        component: StubPage,
        meta: { requiresAuth: true, requiresSectionAccess: true },
      },
    ],
  })
  router.beforeEach(authGuard)
  return router
}

describe('router section access guard', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    localStorage.setItem('selectedInstitutionId', '1')
    vi.clearAllMocks()
  })

  function seedLoggedInInstructor() {
    const userStore = useUserStore()
    userStore.$patch({
      authToken: 'fake-token',
      user: {
        id: 42,
        email: 'instructor@example.com',
        firstName: 'Test',
        lastName: 'Instructor',
        role: 'INSTRUCTOR',
      },
      programs: [{ programId: 1, isAdmin: false, role: 'INSTRUCTOR' }],
    })
    return userStore
  }

  it('redirects to Home when a non-admin cannot access the section', async () => {
    const userStore = seedLoggedInInstructor()
    vi.spyOn(userStore, 'canAccessSection').mockResolvedValue(false)

    const router = createSectionAccessRouter()
    await router.push('/dashboard')
    await router.push('/section/99')

    expect(userStore.canAccessSection).toHaveBeenCalledWith(99)
    expect(router.currentRoute.value.name).toBe('Home')
  })

  it('allows navigation when canAccessSection returns true', async () => {
    const userStore = seedLoggedInInstructor()
    vi.spyOn(userStore, 'canAccessSection').mockResolvedValue(true)

    const router = createSectionAccessRouter()
    await router.push('/dashboard')
    await router.push('/section/10')

    expect(userStore.canAccessSection).toHaveBeenCalledWith(10)
    expect(router.currentRoute.value.name).toBe('Section')
    expect(router.currentRoute.value.params.section_id).toBe('10')
  })

  it('redirects to Home for an invalid section id', async () => {
    const userStore = seedLoggedInInstructor()
    const canAccessSpy = vi.spyOn(userStore, 'canAccessSection')

    const router = createSectionAccessRouter()
    await router.push('/dashboard')
    await router.push('/section/not-a-number')

    expect(canAccessSpy).not.toHaveBeenCalled()
    expect(router.currentRoute.value.name).toBe('Home')
  })
})
