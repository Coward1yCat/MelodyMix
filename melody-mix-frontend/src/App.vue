<!-- src/App.vue -->
<script setup>
import { RouterView, RouterLink } from 'vue-router'
import {
  NConfigProvider, NMessageProvider, NNotificationProvider,
  NDialogProvider, NLoadingBarProvider, NLayout, NLayoutHeader, NLayoutContent,
  NSpace, NButton
} from 'naive-ui';
import { useAuthStore } from './stores/auth.js';
import { onMounted, computed } from 'vue'; // 导入 computed

// 导入 AudioPlayer 组件
import AudioPlayer from '@/components/AudioPlayer.vue';

const authStore = useAuthStore();

const handleLogout = () => {
  authStore.logout();
}

// 辅助函数：根据角色代码返回可读名称
const getRoleDisplayName = (role) => {
    switch (role) {
        case 'ADMIN': return '管理员';
        case 'USER': return '普通用户';
        case 'COMPANY': return '公司用户';
        default: return role;
    }
};

onMounted(() => {
    authStore.initializeAuth();
});

// 计算属性，用于判断是否应该显示上传歌曲入口
const canUploadSongs = computed(() => {
    return authStore.user && (authStore.isAdmin || authStore.isCompany);
});

</script>

<template>
  <n-config-provider>
    <n-message-provider>
    <n-notification-provider>
    <n-dialog-provider>
    <n-loading-bar-provider>
      <n-layout style="height: 100vh;">
        <n-layout-header bordered style="padding: 10px 20px;">
          <!-- 导航栏内容 -->
          <n-space justify="space-between" align="center">
            <router-link to="/" style="font-size: 20px; font-weight: bold; text-decoration: none; color: inherit;">
              MelodyMix
            </router-link>
            <n-space>
              <template v-if="!authStore.isAuthenticated">
                 <router-link to="/login"><n-button text>登录</n-button></router-link>
                 <router-link to="/register"><n-button text>注册</n-button></router-link>
              </template>
              <template v-else>
                <span>欢迎, {{ authStore.user?.username }} ({{ getRoleDisplayName(authStore.user?.role) }})</span>
                <router-link to="/songs"><n-button text>发现音乐</n-button></router-link>
                
                <!-- 只有管理员或公司用户能看到上传歌曲入口 -->
                <router-link 
                  v-if="canUploadSongs"
                  to="/upload">
                  <n-button text>上传歌曲</n-button>
                </router-link>
                
                <router-link to="/my-playlists"><n-button text>我的播放列表</n-button></router-link>
                <router-link to="/profile"><n-button text>个人中心</n-button></router-link>
                
                <router-link to="/dashboard"><n-button text>仪表盘</n-button></router-link>
                <n-button strong secondary type="error" @click="handleLogout">
                  登出
                </n-button>
              </template>
            </n-space>
          </n-space>
        </n-layout-header>

        <!-- 主内容区 -->
        <n-layout-content content-style="padding-bottom: 72px; box-sizing: border-box;">
          <RouterView />
        </n-layout-content>
      </n-layout>

      <!-- AudioPlayer 组件放在布局的末尾 -->
      <AudioPlayer />

    </n-loading-bar-provider>
    </n-dialog-provider>
    </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<style>
/* 确保内容不会被播放器遮挡 */
/* App.vue中设置的 padding-bottom: 72px 已经处理了这个问题 */
</style>
