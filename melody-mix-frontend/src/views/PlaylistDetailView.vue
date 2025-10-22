<!-- src/views/PlaylistDetailView.vue -->
<template>
  <div class="playlist-detail-container">
    <n-spin :show="playlistStore.isLoading" size="large">
      <!-- 如果成功获取到播放列表详情 -->
      <template v-if="playlistStore.playlistDetail">
        <n-card class="playlist-header" :bordered="false">
          <n-space align="center">
            <!-- 播放列表封面，目前使用默认图 -->
            <n-image
              width="120"
              height="120"
              object-fit="cover"
              src="/fallback_album_cover.png"
              fallback-src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg"
              style="border-radius: 8px;"
            />
            <div>
              <h1 class="playlist-title">{{ playlistStore.playlistDetail.name }}</h1>
              <p class="playlist-description">{{ playlistStore.playlistDetail.description || '无描述' }}</p>
              <n-text depth="3">
                {{ playlistStore.playlistDetail.songs?.length || 0 }} 首歌
              </n-text>

              <n-space style="margin-top: 15px;">
                <n-button
                    type="primary"
                    round
                    :disabled="!playlistStore.playlistDetail.songs || playlistStore.playlistDetail.songs.length === 0"
                    @click="handlePlayAll"
                >
                    <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M8 5V19L19 12L8 5Z" fill="currentColor"></path></svg></n-icon></template>
                    播放全部
                </n-button>
                <!-- 可以根据需要添加其他操作，例如“分享”等 -->
              </n-space>
            </div>
          </n-space>
        </n-card>

        <n-divider />

        <h2>歌曲列表</h2>
        <!-- 如果播放列表有歌曲 -->
        <div v-if="hasSongs">
          <n-list bordered hoverable clickable>
            <n-list-item v-for="song in playlistStore.playlistDetail.songs" :key="song.id">
              <n-thing>
                <template #avatar>
                  <n-avatar :src="song.coverUrl || '/fallback_album_cover.png'" />
                </template>
                <template #header>
                  <n-text strong>{{ song.title }}</n-text>
                </template>
                <template #description>
                  <n-space>
                    <n-tag type="info" size="small" round>
                      {{ song.artist }}
                    </n-tag>
                    <n-text depth="3">{{ formatDuration(song.duration) }}</n-text>
                  </n-space>
                </template>
              </n-thing>
              <template #suffix>
                <n-space>
                  <!-- 播放单曲按钮 -->
                  <n-button quaternary circle @click="playerStore.playSong(song)">
                      <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M8 5V19L19 12L8 5Z" fill="currentColor"></path></svg></n-icon></template>
                  </n-button>
                  <!-- 从播放列表移除歌曲按钮 -->
                  <n-popconfirm
                    @positive-click="handleRemoveSong(song.id)"
                    positive-text="确认"
                    negative-text="取消"
                  >
                    <template #trigger>
                      <n-button quaternary circle type="error">
                          <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12L19 6.41Z" fill="currentColor"></path></svg></n-icon></template>
                      </n-button>
                    </template>
                    确定要从播放列表 "{{ playlistStore.playlistDetail.name }}" 中移除 "{{ song.title }}" 吗？
                  </n-popconfirm>
                </n-space>
              </template>
            </n-list-item>
          </n-list>
        </div>
        <!-- 如果播放列表没有歌曲 -->
        <n-empty v-else description="这个播放列表还没有歌曲。">
            <template #extra>
                <n-button size="small" @click="router.push('/songs')">探索歌曲并添加</n-button>
            </template>
        </n-empty>

      </template>
      <!-- 如果未加载到播放列表详情（例如ID无效、无权限或被删除） -->
      <template v-else-if="!playlistStore.isLoading && !playlistStore.playlistDetail">
        <n-empty description="未找到此播放列表，或您没有权限访问。">
            <template #extra>
                <n-button size="small" @click="router.push('/my-playlists')">返回我的播放列表</n-button>
            </template>
        </n-empty>
      </template>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  NSpin, NCard, NSpace, NImage, NText, NButton, NIcon, NDivider,
  NList, NListItem, NThing, NAvatar, NTag, NPopconfirm, NEmpty,
  useMessage // 导入 Naive UI 的消息提示
} from 'naive-ui';
import { usePlaylistStore } from '@/stores/playlist.js';
import { usePlayerStore } from '@/stores/player.js'; // ✅ 导入播放器 store

const route = useRoute();
const router = useRouter();
const message = useMessage(); // 使用 Naive UI 消息提示
const playlistStore = usePlaylistStore();
const playerStore = usePlayerStore(); // ✅ 实例化播放器 store

const playlistId = computed(() => route.params.id); // 从路由参数获取播放列表ID
const hasSongs = computed(() => playlistStore.playlistDetail?.songs?.length > 0);

// 格式化歌曲时长为 MM:SS
const formatDuration = (seconds) => {
  if (typeof seconds !== 'number' || isNaN(seconds)) return '00:00';
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
};

// 获取播放列表详情
const fetchDetail = async () => {
  if (!playlistId.value) {
    message.warning('播放列表ID不合法，正在返回我的播放列表。');
    router.push('/my-playlists');
    return;
  }
  try {
    const detail = await playlistStore.fetchPlaylistDetail(playlistId.value);
    // 如果 store.playlistDetail 为空，说明后端返回404或无权限
    if (!detail && !playlistStore.isLoading) { // 再次检查防止加载中状态
        message.error('无法加载播放列表详情，可能不存在或权限不足。');
        router.replace('/my-playlists'); // 导航回我的播放列表
    }
  } catch (error) {
    // 错误处理已在 store 中完成，这里捕获通常是 re-throw 的错误
    router.replace('/my-playlists'); // 获取失败时返回列表页
  }
};

// 处理“播放全部”按钮点击
const handlePlayAll = () => {
    if (playlistStore.playlistDetail && playlistStore.playlistDetail.songs.length > 0) {
        playerStore.playPlaylist(playlistStore.playlistDetail.songs);
        message.success(`开始播放播放列表 "${playlistStore.playlistDetail.name}"`);
    } else {
        message.warning('此播放列表没有歌曲可播放。');
    }
};

// 从播放列表中移除歌曲
const handleRemoveSong = async (songId) => {
  try {
    await playlistStore.removeSongFromPlaylist(playlistId.value, songId);
    // store 中的 action 已经负责刷新 playlistDetail
  } catch (error) {
    // 错误处理已在 store 中完成
  }
};

// 组件挂载时获取详情
onMounted(() => {
  fetchDetail();
});

// 监听路由参数变化，如果 playlistId 改变，重新获取详情
watch(playlistId, (newId, oldId) => {
  if (newId && newId !== oldId) {
    fetchDetail();
  }
});
</script>

<style scoped>
.playlist-detail-container {
  max-width: 960px;
  margin: 30px auto;
  padding: 20px;
}

.playlist-header {
  margin-bottom: 20px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 增加一点阴影效果 */
  border-radius: 12px;
}

.playlist-title {
  font-size: 2.5em;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.playlist-description {
  font-size: 1.1em;
  color: #666;
  margin-bottom: 10px;
}

.n-list .n-list-item {
    align-items: center; /* 垂直居中列表项内容 */
}

.n-list-item .n-thing__avatar {
    margin-right: 15px;
}
</style>
