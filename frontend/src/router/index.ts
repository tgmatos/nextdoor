import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import {isAuthenticated} from "@/utils/auth.ts";
import DashboardLayout from '../components/layouts/DashboardLayout.vue'
// import * as path from "node:path";
import {name} from "@vue/eslint-config-prettier";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/store/new',
      name: 'store',
      component: () => import('../views/StoreView.vue')
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardLayout,
      children: [
        {
          path: 'dashboard',
          name: 'dashboard-home',
          component: () => import('../views/DashboardHomeView.vue')
        },
        {
          path: 'products',
          name: 'dashboard-product',
          component: () => import('../views/DashboardProductView.vue'),
        },
        {
          path: 'products/new',
          name: 'dashboard-product-new',
          component: () => import('../views/DashboardProductNewView.vue'),
        }
      ]
    },
  ],
});

router.beforeEach((to, from) => {
  const publicRoutes = ['home', 'login', 'register'];
  if(!isAuthenticated() && !publicRoutes.includes(to.name as string)) {
    return {name: 'home'}
  }

  if(publicRoutes.includes(to.name as string) && isAuthenticated()) {
    return {name: 'dashboard'}
  }
});

export default router
