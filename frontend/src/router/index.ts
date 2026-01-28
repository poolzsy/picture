import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import UserLogin from '@/views/user/userLogin.vue'
import UserRegister from '@/views/user/userRegister.vue'
import userManage from '@/views/admin/userManage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
    },
    {
      path: '/admin/userManage',
      name: 'userManage',
      component: userManage,
    },
    {
      path: '/user/login',
      name: 'userLogin',
      component: UserLogin,
    },
    {
      path: '/user/register',
      name: 'userRegister',
      component: UserRegister,
    },
  ],
})

export default router
