import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
    },
    {
      path: '/ratings',
      name: 'ratings',
      component: () => import('@/views/RatingsView.vue'),
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/views/SearchView.vue'),
    },
    {
      path: '/movies',
      name: 'movies',
      component: () => import('@/views/MoviesView.vue'),
    },
    {
      path: '/movies/:id',
      name: 'movie-detail',
      component: () => import('@/views/MovieDetailView.vue'),
      meta: { scrollToTop: true },
    },
    {
      path: '/series',
      name: 'series',
      component: () => import('@/views/SeriesView.vue'),
    },
    {
      path: '/series/:id',
      name: 'series-detail',
      component: () => import('@/views/SeriesDetailView.vue'),
      meta: { scrollToTop: true },
    },
    {
      path: '/broken',
      name: 'broken',
      component: () => import('@/views/BrokenView.vue'),
    },
    {
      path: '/agent',
      name: 'agent',
      component: () => import('@/views/AgentView.vue'),
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/SettingsView.vue'),
    },
    {
      path: '/jobs',
      name: 'jobs',
      component: () => import('@/views/JobsView.vue'),
    },
    {
      path: '/logs',
      name: 'logs',
      component: () => import('@/views/LogsView.vue'),
    },
    {
      path: '/how-to',
      name: 'howto',
      component: () => import('@/views/HowToView.vue'),
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.scrollToTop) {
    document.querySelector('.app-layout__main')?.scrollTo({ top: 0 })
  }
})

export default router
