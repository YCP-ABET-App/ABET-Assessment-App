import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user-store'

let setupChecked = false
let needsSetup = false

export function markSetupComplete() {
  needsSetup = false
  setupChecked = true
}

export async function authGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const userStore = useUserStore()

  if (!setupChecked) {
    try {
      const res = await fetch('/api/setup/status')
      const data = await res.json()
      needsSetup = data.needsSetup
    } catch {
      needsSetup = false
    }
    setupChecked = true
  }

  if (needsSetup && to.name !== 'First Time Setup') {
    next({ name: 'First Time Setup' })
    return
  }
  if (!needsSetup && to.name === 'First Time Setup') {
    next({ name: 'Institution Login' })
    return
  }

  if (!userStore.isLoggedIn && localStorage.getItem('authToken')) {
    userStore.loadFromStorage()
  }

  const institutionId = localStorage.getItem('selectedInstitutionId')
  const requiresAuth = !!to.meta.requiresAuth
  const requiresAdmin = !!to.meta.requiresAdmin
  const requiresMatchingInstructorId = !!to.meta.requiresMatchingInstructorId
  const requiresSectionAccess = !!to.meta.requiresSectionAccess

  // Logged-in users leave both gates
  if (userStore.isLoggedIn && (to.name === 'Institution Login' || to.name === 'Login')) {
    next({ name: 'Home' })
    return
  }

  // Institution Login is always reachable when not logged in
  // (so from Login you can go back and enter a different institution)
  if (to.name === 'Institution Login') {
    next()
    return
  }

  // Block Login until institution has been verified
  if (to.name === 'Login' && !institutionId) {
    next({ name: 'Institution Login', query: { redirect: to.fullPath } })
    return
  }

  // Block protected routes the same way
  if (requiresAuth && !userStore.isLoggedIn) {
    if (!institutionId) {
      next({ name: 'Institution Login', query: { redirect: to.fullPath } })
    } else {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    }
    return
  }

  if (requiresMatchingInstructorId && !userStore.isAdmin) {
    const instructorId = userStore.userId
    const routeId = Number(to.params.instructor_id)
    if (instructorId != routeId) {
      next({ name: 'Home' })
      return
    }
  }

  if (requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Home' })
    return
  }

  if (requiresSectionAccess) {
    const sectionId = Number(to.params.section_id)
    if (!sectionId || !(await userStore.canAccessSection(sectionId))) {
      next({ name: 'Home' })
      return
    }
  }

  next()
}