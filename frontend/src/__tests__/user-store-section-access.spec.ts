import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user-store'
import api from '@/api'

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

describe('user-store canAccessSection', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  function seedInstructor(overrides?: { programs?: { programId: number; isAdmin: boolean; role: 'ADMIN' | 'INSTRUCTOR' }[] }) {
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
      programs: overrides?.programs ?? [
        { programId: 1, isAdmin: false, role: 'INSTRUCTOR' },
      ],
    })
    return userStore
  }

  it('returns false when sectionId or userId is missing', async () => {
    const userStore = useUserStore()
    expect(await userStore.canAccessSection(0)).toBe(false)
    expect(await userStore.canAccessSection(5)).toBe(false)
    expect(api.get).not.toHaveBeenCalled()
  })

  it('allows access when the user is linked to the section', async () => {
    const userStore = seedInstructor()
    vi.mocked(api.get).mockResolvedValueOnce({
      data: { data: [{ id: 1, sectionId: 10, userId: 42 }] },
    })

    await expect(userStore.canAccessSection(10)).resolves.toBe(true)
    expect(api.get).toHaveBeenCalledWith('/section-user', {
      params: { sectionId: 10, userId: 42 },
    })
    expect(api.get).toHaveBeenCalledTimes(1)
  })

  it('allows access when the user is admin of a program tied to the section', async () => {
    const userStore = seedInstructor({
      programs: [
        { programId: 7, isAdmin: true, role: 'ADMIN' },
        { programId: 3, isAdmin: false, role: 'INSTRUCTOR' },
      ],
    })

    vi.mocked(api.get)
      .mockResolvedValueOnce({ data: { data: [] } })
      .mockResolvedValueOnce({
        data: { data: [{ id: 1, sectionId: 10, programId: 7 }] },
      })

    await expect(userStore.canAccessSection(10)).resolves.toBe(true)
    expect(api.get).toHaveBeenCalledWith('/section-program', {
      params: { sectionId: 10 },
    })
  })

  it('denies access when the user is not the instructor and not admin of a linked program', async () => {
    const userStore = seedInstructor({
      programs: [{ programId: 1, isAdmin: false, role: 'INSTRUCTOR' }],
    })

    vi.mocked(api.get)
      .mockResolvedValueOnce({ data: { data: [] } })
      .mockResolvedValueOnce({
        data: { data: [{ id: 1, sectionId: 10, programId: 99 }] },
      })

    await expect(userStore.canAccessSection(10)).resolves.toBe(false)
  })

  it('denies access when APIs fail', async () => {
    const userStore = seedInstructor()
    vi.mocked(api.get).mockRejectedValueOnce(new Error('network error'))
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    await expect(userStore.canAccessSection(10)).resolves.toBe(false)
    consoleSpy.mockRestore()
  })
})
