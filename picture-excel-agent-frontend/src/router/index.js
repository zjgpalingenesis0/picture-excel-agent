import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ImageToExcel from '../views/ImageToExcel.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/image-to-excel',
    name: 'ImageToExcel',
    component: ImageToExcel
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
