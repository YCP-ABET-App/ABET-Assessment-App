import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user-store'

export async function authGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const userStore = useUserStore()
  const institutionId = localStorage.getItem('selectedInstitutionId')

  // Load user data from storage if not already loaded
  if (!userStore.isLoggedIn && localStorage.getItem('authToken')) {
    userStore.loadFromStorage()
  }

  const requiresAuth = to.meta.requiresAuth
  const requiresAdmin = to.meta.requiresAdmin
  const requiresMatchingInstructorId = to.meta.requiresMatchingInstructorId
  const requiresSectionAccess = to.meta.requiresSectionAccess

  // Allow institution login page for all users
  if (to.name === 'Institution Log In') {
    next()
    return
  }

  // If user is logged in but trying to access login/signup pages, redirect to dashboard
  if (userStore.isLoggedIn && (to.name === 'Log In')) {
    next({ name: 'Home' })
    return
  }

  // If user is not logged in and trying to access login/signup, ensure institution is selected
  if (!userStore.isLoggedIn && (to.name === 'Log In')) {
    if (!institutionId) {
      next({
        name: 'Institution Log In',
        query: { redirect: to.fullPath }
      })
      return
    }
    // Institution is selected, allow access to login/signup
    next()
    return
  }

  // If user is not logged in and trying to access a protected route
  if (requiresAuth && !userStore.isLoggedIn) {
    // If institution not selected, go to institution login
    if (!institutionId) {
      next({
        name: 'Institution Log In',
        query: { redirect: to.fullPath }
      })
      return
    }
    // If institution selected but not logged in, go to login page
    next({
      name: 'Log In',
      query: { redirect: to.fullPath }
    })
    return
  }

  if (requiresMatchingInstructorId && !userStore.isAdmin) {
    const instructorId = userStore.userId;
    const routeId = Number(to.params.instructorId)
    if (instructorId != routeId) {
      next({ name: 'Home' })
      return
    }
  }

  // If user is logged in but trying to access institution login, redirect to dashboard
  if (userStore.isLoggedIn && to.name === 'Institution Log In') {
    next({ name: 'Home' })
    return
  }

  // Handle admin routes
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
