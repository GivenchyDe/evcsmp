import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: () => import('@/views/layout/LayoutView.vue'),
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: '首页概览' } },
        { path: 'user', component: () => import('@/views/user/UserView.vue'), meta: { title: '用户管理' } },
        { path: 'station', component: () => import('@/views/station/StationView.vue'), meta: { title: '网点管理' } },
        { path: 'station-detail/:id', component: () => import('@/views/station/StationDetailView.vue'), meta: { title: '充电站详情' } },
        { path: 'charger', component: () => import('@/views/charger/ChargerView.vue'), meta: { title: '充电桩管理' } },
        { path: 'gun', component: () => import('@/views/gun/GunView.vue'), meta: { title: '充电枪管理' } },
        { path: 'billing', component: () => import('@/views/billing/BillingView.vue'), meta: { title: '计费规则' } },
        { path: 'order', component: () => import('@/views/order/OrderView.vue'), meta: { title: '充电订单' } },
        { path: 'recharge', component: () => import('@/views/recharge/RechargeView.vue'), meta: { title: '充值订单' } },
        { path: 'review', component: () => import('@/views/review/ReviewView.vue'), meta: { title: '订单评价' } },
        { path: 'feedback', component: () => import('@/views/feedback/FeedbackView.vue'), meta: { title: '故障反馈' } },
        { path: 'announcement', component: () => import('@/views/announcement/AnnouncementView.vue'), meta: { title: '公告管理' } },
        { path: 'message', component: () => import('@/views/message/MessageView.vue'), meta: { title: '消息通知' } },
        { path: 'chat', component: () => import('@/views/chat/ChatView.vue'), meta: { title: '客服聊天' } },
        { path: 'admin', component: () => import('@/views/admin/AdminView.vue'), meta: { title: '管理员' } },
        { path: 'monitor', component: () => import('@/views/monitor/MonitorView.vue'), meta: { title: '实时监控' } },
        { path: 'statistics', component: () => import('@/views/statistics/StatisticsView.vue'), meta: { title: '数据统计' } },
        { path: 'message-push', component: () => import('@/views/messagepush/MessagePushView.vue'), meta: { title: '消息推送' } },
      ],
    },
  ],
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
