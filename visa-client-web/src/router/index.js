import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/layout/index.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/Home.vue')
      },
      {
        path: 'products',
        component: () => import('@/views/ProductList.vue')
      },
      {
        path: 'product/:id', // 动态路由，id是参数
        component: () => import('@/views/ProductDetail.vue')
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { requireAuth: true } // 标记需要登录(后续可在路由守卫里用)
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'), // 确保你的文件路径是这个
        meta: { title: '预选清单' }
      },
      {
        path: 'order/create',
        name: 'OrderCreate',
        component: () => import('@/views/OrderCreate.vue'), // 确保文件路径正确
        meta: { title: '确认订单' }
      },
      {
        path: 'user/orders',
        name: 'UserOrder',
        component: () => import('@/views/user/UserOrder.vue'),
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'order/upload',
        name: 'OrderUpload',
        component: () => import('@/views/user/Upload.vue'),
        meta: { title: '上传材料' }
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/ai/index.vue'),
        meta: { title: '智能助手' }
      }

    ]
  },
  {
    path: '/login',
    // 登录页不需要顶部导航，所以不套 Layout，直接显示
    component: () => import('@/views/Login.vue')
  }
]

const router = new VueRouter({
  mode: 'history', // 去掉 URL 里的 #
  routes
})

export default router
