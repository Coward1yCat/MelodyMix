// src/router/index.js

import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth.js'; // ✅ 关键修改：直接在文件顶部导入 useAuthStore

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/songs',
    name: 'SongList',
    component: () => import('../views/SongListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('../views/UploadView.vue'), // 惰性加载
    meta: {
      requiresAuth: true,
      roles: ['ADMIN', 'COMPANY'] // 定义访问此页面所需要的角色
    }
  },
  {
    path: '/my-playlists',
    name: 'MyPlaylists',
    component: () => import('@/views/MyPlaylistsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/playlists/:id',
    name: 'PlaylistDetail',
    component: () => import('@/views/PlaylistDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 全局前置守卫 (Navigation Guard)
router.beforeEach((to, from, next) => {
  // ✅ 关键修改：移除旧的间接获取方式，直接调用 useAuthStore() 获取 store 实例
  const authStore = useAuthStore();

  const requiresAuth = to.meta.requiresAuth;
  const requiredRoles = to.meta.roles;

  // 场景一：页面需要认证但用户未登录
  if (requiresAuth && !authStore.isAuthenticated) {
    if (window.$message) window.$message.warning('请先登录才能访问此页面。'); // 提示
    next('/login');
    return;
  }

  // 场景二：页面需要特定角色，但用户角色不匹配
  if (requiredRoles && requiredRoles.length > 0) {
      if (!authStore.user || !requiredRoles.includes(authStore.user.role)) {
          const userRole = authStore.user?.role || '无';
          console.warn(`权限不足：用户角色 '${userRole}' 无法访问需要 '${requiredRoles.join(', ')}' 角色的页面。`);
          if (window.$message) window.$message.error('您没有权限访问此页面。'); // 更友好的提示
          next('/dashboard'); // 或者 next('/')
          return;
      }
  }

  // 场景三：用户已登录，但尝试访问登录或注册页
  if ((to.name === 'Login' || to.name === 'Register') && authStore.isAuthenticated) {
    next('/dashboard');
    return;
  }

  // 所有检查通过，允许访问
  next();
});

export default router;
