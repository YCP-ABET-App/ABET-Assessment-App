import { createRouter, createMemoryHistory } from 'vue-router'

/**
 * Creates a router instance for testing
 * Uses memory history to avoid browser dependencies
 */
export function createTestRouter(initialPath = '/dashboard') {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/dashboard',
        name: 'Home',
        component: { template: '<div>Home</div>' },
      },
      {
        path: '/admin-dashboard',
        name: 'Admin Dashboard',
        component: { template: '<div>Admin</div>' },
      },
      {
        path: '/institution-login',
        name: 'Institution Log In',
        component: { template: '<div>Institution Login</div>' },
      },
      {
        path: '/test-connection',
        name: 'TestConnection',
        component: { template: '<div>Test</div>' },
      },
      {
        path: '/login',
        name: 'Login',
        component: { template: '<div>Login</div>' },
      },
    ],
  })

  router.push(initialPath)
  return router
}

/**
 * Router stubs for components that use router-link or router-view
 * Use this when you don't need actual routing functionality
 */
export const routerStubs = {
  'router-link': {
    template: '<a><slot /></a>',
  },
  'router-view': {
    template: '<div><slot /></div>',
  },
}
