import { createRouter, createWebHistory } from 'vue-router'
import { authGuard } from '@/router/guards'
import ConnectionTest from '@/components/ConnectionTest.vue'
import HomePage from '@/components/pages/HomePage.vue'
import SummaryPage from '@/components/pages/SummaryPage.vue'
import FCARPage from '@/components/pages/FCARPage.vue'
import SectionViewPage from '@/components/pages/SectionViewPage.vue'
import InstructorViewPage from '@/components/pages/InstructorViewPage.vue'
import ProgramCoursesPage from '@/components/pages/ProgramCoursesPage.vue'
import ProgramInstructorsPage from '@/components/pages/ProgramInstructorsPage.vue'
import LoginPage from '@/components/pages/LogIn.vue';
import ManagementPage from "@/components/ManagementPage.vue";
import InstitutionLoginPage from "@/components/pages/InstitutionLoginPage.vue";
import MultiYearReportPage from "@/components/pages/MultiYearReportPage.vue";
import SchedulePage from '@/components/pages/SchedulePage.vue';
import AccountRecoveryPage from '@/components/pages/AccountRecoveryPage.vue'
import FirstTimeSetup from '@/components/pages/SetupPage.vue'
import SuperAdminPage from "@/components/pages/SuperAdminPage.vue";




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
    meta: { requiresAuth: true, requiresAdmin: true }
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
    meta: { requiresAuth: true, requiresSectionAccess: true }
  },
  {
    path: '/instructor/:instructor_id',
    name: 'Instructor',
    component: InstructorViewPage,
    meta: { requiresAuth: true, requiresMatchingInstructorId: true }
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
    name: 'Login',
    component: LoginPage,
  },
  {
    path: "/reset-password",
    name: "Reset Password",
    component: AccountRecoveryPage
  },
  {
    path: "/first-setup",
    name: "First Time Setup",
    component: FirstTimeSetup
  },
  {
    path: '/institution-login',
    name: 'Institution Login',
    component: InstitutionLoginPage,
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
  },
  {
    path: '/:program_id/multi-year-summary',
    name: 'Multi-Year Summary',
    component: MultiYearReportPage,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    // This creates a dedicated URL for schedule entry page
    path: '/:programId/schedule-entry',
    name: 'ScheduleEntry',
    component: SchedulePage,
    meta: { requiresAuth: true, requiresAdmin: true },
    props: true
  },
  {
    path: '/super-admin', name: 'SuperAdmin',
    component: SuperAdminPage,
    meta: {
      requiresAuth: true,
      requiresSuperAdmin: true,
    },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routes,
})



router.beforeEach(authGuard) // Apply the authGuard to all routes


export default router

