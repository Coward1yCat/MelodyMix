<!-- src/views/SongListView.vue -->
<template>
  <div class="song-list-container">
    <h1>发现音乐</h1>

    <!-- ✅ 新增起始：搜索框和按钮 -->
    <n-space justify="space-between" align="center" style="margin-bottom: 20px;">
      <n-input
        v-model:value="searchQuery"
        placeholder="搜索歌曲标题、艺术家或专辑"
        style="width: 300px;"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleClearSearch"
      >
        <template #suffix>
          <n-icon @click="handleSearch" style="cursor: pointer;">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z" fill="currentColor"></path></svg>
          </n-icon>
        </template>
      </n-input>
      <n-button type="primary" @click="handleSearch" :loading="loading">搜索</n-button>
      <n-button v-if="searchQuery" @click="handleClearSearch">清空</n-button>
    </n-space>
    <!-- ⛔️ 新增结束 -->

    <!-- 列表和分页 -->
    <n-spin :show="loading" size="large">
      <div v-if="!loading && songs.length > 0">
        <n-list bordered hoverable clickable>
          <n-list-item v-for="song in songs" :key="song.id">
            <!-- 歌曲封面 -->
            <template #prefix>
              <n-avatar :size="48" :src="song.coverUrl || '/default-cover.png'" object-fit="cover" />
            </template>
            
            <!-- 歌曲信息 -->
            <n-thing :title="song.title" :description="`${song.artist} - ${song.album || '未知专辑'}`" />
            
            <!-- 操作按钮 -->
            <template #suffix>
              <n-space>
                <!-- 播放按钮，现在 playerStore 会是可用的 -->
                <n-button type="primary" size="small" @click="playerStore.playSong(song)">播放</n-button>
                
                <!-- 喜欢按钮 (已实现) -->
                <n-button 
                  size="small"
                  :type="userStore.isSongLiked(song.id) ? 'success' : 'default'" 
                  :ghost="!userStore.isSongLiked(song.id)"
                  @click="userStore.toggleLike(song.id)">
                  {{ userStore.isSongLiked(song.id) ? '已喜欢' : '喜欢' }}
                </n-button>
                
                <!-- 添加到播放列表按钮 -->
                <n-button size="small" @click="openAddToPlaylistModal(song.id)">
                  添加到播放列表
                </n-button>
              </n-space>
            </template>
          </n-list-item>
        </n-list>
        
        <!-- 分页控件 -->
        <n-pagination
          v-if="paginationInfo.totalPages > 1"
          v-model:page="paginationInfo.currentPage"
          :page-count="paginationInfo.totalPages"
          @update:page="handlePageChange"
          size="large"
          style="margin-top: 20px; justify-content: center;"
        />
      </div>
      
      <!-- 空状态 -->
      <n-empty v-if="!loading && songs.length === 0" description="曲库空空如也，快去上传一些音乐吧！" />
    </n-spin>
    
    <!-- 添加到播放列表的模态框 -->
    <n-modal
      v-model:show="showModal"
      preset="card"
      style="width: 600px"
      title="添加到播放列表"
      :bordered="false"
      size="huge"
    >
      <n-spin :show="playlistStore.isLoading">
        <!-- 如果有播放列表，则显示列表 -->
        <div v-if="playlistStore.myPlaylists.length > 0">
          <n-list hoverable clickable>
            <n-list-item v-for="playlist in playlistStore.myPlaylists" :key="playlist.id" @click="handleAddToPlaylist(playlist.id)">
              {{ playlist.name }}
              <template #suffix>
                <n-text depth="3">{{ playlist.songs ? playlist.songs.length : 0}} 首歌</n-text>
              </template>
            </n-list-item>
          </n-list>
        </div>
        <!-- 如果没有播放列表，则显示空状态 -->
        <div v-else>
          <n-empty description="你还没有创建任何播放列表。">
            <template #extra>
              <!-- 未来可以链接到创建播放列表的页面 -->
              <n-button size="small">去创建</n-button>
            </template>
          </n-empty>
        </div>
      </n-spin>
    </n-modal>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from 'axios';
import { 
  NSpin, NList, NListItem, NThing, NAvatar, NButton, NSpace, NPagination, NEmpty, 
  NModal, NText, NInput, NIcon // ✅ 导入 NInput 和 NIcon
} from 'naive-ui';
import { useUserStore } from '@/stores/user.js';
import { usePlaylistStore } from '@/stores/playlist.js';
import { usePlayerStore } from '@/stores/player.js'; 

const userStore = useUserStore();
const playlistStore = usePlaylistStore();
const playerStore = usePlayerStore(); 

// --- 列表状态 (分页等) ---
const songs = ref([]);
const loading = ref(true);
const paginationInfo = reactive({
  currentPage: 1,
  pageSize: 10,
  totalElements: 0,
  totalPages: 1,
});

const searchQuery = ref(''); // ✅ 新增搜索查询状态

// 模态框所需的状态
const showModal = ref(false); // 控制模态框的显示与隐藏
const selectedSongId = ref(null); // 记录当前操作的是哪首歌

// --- 方法：获取歌曲列表 (修改为接受搜索参数) ---
const fetchSongs = async (page = 1, query = '') => { // ✅ 添加 query 参数
  loading.value = true;
  try {
    const response = await axios.get('/songs', { 
      params: {
        page: page - 1,
        size: paginationInfo.pageSize,
        title: query, // ✅ 将搜索关键词传递给后端
      },
    });
    
    const data = response.data;
    if (data) { 
        songs.value = data.content;
        paginationInfo.currentPage = data.number + 1;
        paginationInfo.totalElements = data.totalElements;
        paginationInfo.totalPages = data.totalPages;
    } else {
        songs.value = [];
    }
  } catch (error) {
    console.error('获取歌曲列表失败:', error);
    if (window.$message) {
      window.$message.error('加载歌曲失败，请稍后重试。');
    }
  } finally {
    loading.value = false;
  }
};

// --- 方法：处理翻页 (保持不变) ---
const handlePageChange = (newPage) => {
  fetchSongs(newPage, searchQuery.value); // ✅ 翻页时带上当前搜索关键词
};

// ✅ 新增：处理搜索按钮点击
const handleSearch = () => {
  paginationInfo.currentPage = 1; // 搜索时重置回第一页
  fetchSongs(1, searchQuery.value);
};

// ✅ 新增：处理清空搜索
const handleClearSearch = () => {
  searchQuery.value = '';
  paginationInfo.currentPage = 1;
  fetchSongs(1, ''); // 清空搜索后重新加载所有歌曲
};


// 与模态框交互的方法
// 打开模态框
const openAddToPlaylistModal = (songId) => {
  selectedSongId.value = songId;      // 记录当前点击的是哪首歌
  showModal.value = true;             // 打开模态框
  playlistStore.fetchMyPlaylists();   // 触发获取播放列表的 action
};

// 在模态框中点击某个播放列表后的处理函数
const handleAddToPlaylist = async (playlistId) => {
  if (!selectedSongId.value) return;

  // 调用 store 中的 action
  const success = await playlistStore.addSongToPlaylist(playlistId, selectedSongId.value);
  
  if (success) {
    showModal.value = false;      // 如果添加成功，关闭模态框
    selectedSongId.value = null;  // 清空选择
  }
};

// --- 组件挂载时，获取初始数据 ---
onMounted(async () => {
  await userStore.fetchLikedSongs(); // 获取喜欢的歌曲状态
  await fetchSongs(1);               // 获取第一页的歌曲列表
});
</script>

<style scoped>
.song-list-container {
  max-width: 960px;
  margin: 30px auto;
  padding: 20px;
}
</style>
