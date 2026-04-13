import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user-store'
import ConnectionTest from '@/components/ConnectionTest.vue'
import HomePage from '@/components/pages/HomePage.vue'
import SummaryPage from '@/components/pages/SummaryPage.vue'
import FCARPage from '@/components/pages/FCARPage.vue'
import SectionViewPage from '@/components/pages/SectionViewPage.vue'
import InstructorViewPage from '@/components/pages/InstructorViewPage.vue'
import ProgramCoursesPage from '@/components/pages/ProgramCoursesPage.vue'
import ProgramInstructorsPage from '@/components/pages/ProgramInstructorsPage.vue'
import LogInPage from '@/components/pages/LogIn.vue'
import SignUpPage from '@/components/pages/SignUp.vue'
import ManagementPage from "@/components/ManagementPage.vue";
import InstitutionLoginPage from "@/components/pages/InstitutionLoginPage.vue";

const routes = [
  {
    path: '/',
    redirect: '/institution-login',
  },
  {
    path: '/test-connection',
    name: 'ConnectionTest',
    component: ConnectionTest,
  },
{
    path: '/admin-dashboard',
    name: 'Admin Dashboard',
    component: () => import('@/components/pages/AdminDashboardPage.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/dashboard',
    name: 'Home',
    component: HomePage,
    meta: { requiresAuth: true }
  },
  {
    path: '/:program_id/:semester_id/summary/',
    name: 'Summary',
    component: SummaryPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/fcar/:measure_id',
    name: 'FCAR',
    component: FCARPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/section/:section_id',
    name: 'Section',
    component: SectionViewPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/instructor/:instructor_id',
    name: 'Instructor',
    component: InstructorViewPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/:program_id/courses',
    name: 'Program Courses',
    component: ProgramCoursesPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/:program_id/instructors',
    name: 'Program Instructors',
    component: ProgramInstructorsPage,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Log In',
    component: LogInPage,
  },
  {
    path: '/institution-login',
    name: 'Institution Log In',
    component: InstitutionLoginPage,
  },
  {
    path: '/signup',
    name: 'Sign Up',
    component: SignUpPage,
  },
  {
    path: '/setup',
    name: 'Setup',
    component: ManagementPage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/components/pages/SettingsPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/import-tool',
    name: 'Import Tool',
    component: () => import('@/components/pages/ImportToolPage.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routes,
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const institutionId = localStorage.getItem('selectedInstitutionId')

  // Load user data from storage if not already loaded
  if (!userStore.isLoggedIn && localStorage.getItem('authToken')) {
    userStore.loadFromStorage()
  }

  const requiresAuth = to.meta.requiresAuth
  const requiresAdmin = to.meta.requiresAdmin

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

  // If user is logged in but trying to access institution or auth pages, redirect to dashboard
  if (userStore.isLoggedIn && (to.name === 'Log In' || to.name === 'Sign Up' || to.name === 'Institution Log In')) {
    next({ name: 'Home' })
    return
  }

  // If not logged in and trying to access login/signup, ensure institution is selected
  if (!userStore.isLoggedIn && (to.name === 'Log In' || to.name === 'Sign Up') && !institutionId) {
    next({ name: 'Institution Log In' })
    return
  }

  // Handle admin routes
  if (requiresAdmin && !userStore.isAdmin) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
