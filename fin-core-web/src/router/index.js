import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue') },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue'),
        meta: { permission: 'order:view' }
      },
      {
        path: 'fee-rules',
        name: 'FeeRules',
        component: () => import('@/views/FeeRules.vue'),
        meta: { permission: 'order:view' }
      },
      {
        path: 'settlements',
        name: 'Settlements',
        component: () => import('@/views/Settlements.vue'),
        meta: { permission: 'order:view' }
      },
      {
        path: 'receipts',
        name: 'Receipts',
        component: () => import('@/views/Receipts.vue'),
        meta: { permission: 'payment:view' }
      },
      {
        path: 'payments',
        name: 'Payments',
        component: () => import('@/views/Payments.vue'),
        meta: { permission: 'payment:view' }
      },
      {
        path: 'bills',
        name: 'Bills',
        component: () => import('@/views/Bills.vue'),
        meta: { permission: 'payment:view' }
      },
      {
        path: 'approvals',
        name: 'Approvals',
        component: () => import('@/views/Approvals.vue'),
        meta: { permission: 'payment:view' }
      },
      {
        path: 'accounts',
        name: 'Accounts',
        component: () => import('@/views/Accounts.vue'),
        meta: { permission: 'fund:view' }
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: () => import('@/views/Transactions.vue'),
        meta: { permission: 'fund:view' }
      },
      {
        path: 'reconciliations',
        name: 'Reconciliations',
        component: () => import('@/views/Reconciliations.vue'),
        meta: { permission: 'fund:view' }
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/Reports.vue'),
        meta: { permission: 'report:view' }
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return next('/login')
  }
  if (to.meta.permission && !auth.hasPermission(to.meta.permission)) {
    return next('/')
  }
  next()
})

export default router
