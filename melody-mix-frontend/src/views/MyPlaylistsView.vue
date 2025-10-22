<!-- src/views/MyPlaylistsView.vue -->
<template>
  <div class="my-playlists-container">
    <h1>我的播放列表</h1>

    <n-space justify="end" style="margin-bottom: 20px;">
      <n-button type="primary" @click="openCreateModal">
        <template #icon><n-icon><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M11 11V5H13V11H19V13H13V19H11V13H5V11H11Z" fill="currentColor"></path></svg></n-icon></template>
        创建新播放列表
      </n-button>
    </n-space>

    <n-spin :show="playlistStore.isLoading" size="large">
      <!-- 如果有播放列表则显示列表 -->
      <div v-if="!playlistStore.isLoading && playlistStore.myPlaylists.length > 0">
        <n-list bordered hoverable clickable>
          <n-list-item v-for="playlist in playlistStore.myPlaylists" :key="playlist.id">
            <n-thing :title="playlist.name" :description="playlist.description || '无描述'">
              <template #header-extra>
                <n-text depth="3">{{ playlist.songs ? playlist.songs.length : 0 }} 首歌</n-text>
              </template>
            </n-thing>
            <template #suffix>
              <n-space>
                <n-button size="small" @click="goToPlaylistDetail(playlist.id)">查看详情</n-button>
                <n-button size="small" @click="openEditModal(playlist)">编辑</n-button>
                <n-popconfirm
                  @positive-click="handleDeletePlaylist(playlist.id)"
                  positive-text="确认"
                  negative-text="取消"
                >
                  <template #trigger>
                    <n-button size="small" type="error">删除</n-button>
                  </template>
                  确定要删除播放列表 "{{ playlist.name }}" 吗？此操作不可逆。
                </n-popconfirm>
              </n-space>
            </template>
          </n-list-item>
        </n-list>
      </div>
      <!-- 如果没有播放列表则显示空状态 -->
      <n-empty v-else-if="!playlistStore.isLoading" description="您还没有创建任何播放列表。">
        <template #extra>
          <n-button size="small" type="primary" @click="openCreateModal">
             创建第一个播放列表
          </n-button>
        </template>
      </n-empty>
    </n-spin>

    <!-- 创建播放列表模态框 -->
    <n-modal
      v-model:show="showCreateModal"
      preset="card"
      style="width: 500px"
      title="创建新播放列表"
      :bordered="false"
      @after-leave="() => createFormRef?.restoreValidation()" <!-- 模态框关闭后清除验证状态 -->
    >
      <n-form
        ref="createFormRef"
        :model="newPlaylistForm"
        :rules="createFormRules"
        label-placement="top"
      >
        <n-form-item label="名称" path="name">
          <n-input v-model:value="newPlaylistForm.name" placeholder="播放列表名称" />
        </n-form-item>
        <n-form-item label="描述 (可选)" path="description">
          <n-input
            v-model:value="newPlaylistForm.description"
            type="textarea"
            placeholder="播放列表描述"
            :autosize="{ minRows: 2, maxRows: 5 }"
          />
        </n-form-item>
        <n-form-item>
          <n-button
            type="primary"
            block
            :loading="playlistStore.isLoading"
            @click="handleCreatePlaylist"
          >
            创建
          </n-button>
        </n-form-item>
      </n-form>
    </n-modal>

    <!-- 编辑播放列表模态框 -->
    <n-modal
      v-model:show="showEditModal"
      preset="card"
      style="width: 500px"
      title="编辑播放列表"
      :bordered="false"
      @after-leave="() => editFormRef?.restoreValidation()" <!-- 模态框关闭后清除验证状态 -->
    >
      <n-form
        ref="editFormRef"
        :model="editingPlaylistForm"
        :rules="editFormRules"
        label-placement="top"
      >
        <n-form-item label="名称" path="name">
          <n-input v-model:value="editingPlaylistForm.name" placeholder="播放列表名称" />
        </n-form-item>
        <n-form-item label="描述 (可选)" path="description">
          <n-input
            v-model:value="editingPlaylistForm.description"
            type="textarea"
            placeholder="播放列表描述"
            :autosize="{ minRows: 2, maxRows: 5 }"
          />
        </n-form-item>
        <n-form-item>
          <n-button
            type="primary"
            block
            :loading="playlistStore.isLoading"
            @click="handleUpdatePlaylist"
          >
            保存修改
          </n-button>
        </n-form-item>
      </n-form>
    </n-modal>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import {
  NSpace, NButton, NList, NListItem, NThing, NText, NIcon, NPopconfirm,
  NSpin, NEmpty, NModal, NForm, NFormItem, NInput,
  useMessage // 导入 Naive UI 的消息提示
} from 'naive-ui';
import { usePlaylistStore } from '@/stores/playlist.js';

const router = useRouter(); // 路由实例
const message = useMessage(); // 消息提示实例 (通过 createDiscreteApi 注入到全局)
const playlistStore = usePlaylistStore();

// --- 创建播放列表模态框状态与表单 ---
const showCreateModal = ref(false);
const createFormRef = ref(null); // 用于表单验证的 ref
const newPlaylistForm = reactive({
  name: '',
  description: '',
});
const createFormRules = {
  name: [{ required: true, message: '请填写播放列表名称', trigger: ['blur', 'input'] }]
};

// 打开创建模态框
const openCreateModal = () => {
  newPlaylistForm.name = ''; // 重置表单字段
  newPlaylistForm.description = '';
  showCreateModal.value = true;
  // nextTick(() => createFormRef.value?.restoreValidation()); // 在打开后也清除验证状态
};

// 处理创建播放列表
const handleCreatePlaylist = async () => {
  createFormRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        await playlistStore.createPlaylist(newPlaylistForm.name, newPlaylistForm.description);
        showCreateModal.value = false; // 成功后关闭模态框
      } catch (error) {
        // 错误处理已在 store 中统一完成
      }
    } else {
      message.error('请填写完整的播放列表信息');
    }
  });
};

// --- 编辑播放列表模态框状态与表单 ---
const showEditModal = ref(false);
const editFormRef = ref(null); // 用于表单验证的 ref
const editingPlaylistForm = reactive({
  id: null,
  name: '',
  description: '',
});
const editFormRules = {
  name: [{ required: true, message: '请填写播放列表名称', trigger: ['blur', 'input'] }]
};

// 打开编辑模态框
const openEditModal = (playlist) => {
  editingPlaylistForm.id = playlist.id;
  editingPlaylistForm.name = playlist.name;
  editingPlaylistForm.description = playlist.description;
  showEditModal.value = true;
  // nextTick(() => editFormRef.value?.restoreValidation()); // 在打开后也清除验证状态
};

// 处理更新播放列表
const handleUpdatePlaylist = async () => {
  editFormRef.value?.validate(async (errors) => {
    if (!errors) {
      try {
        await playlistStore.updatePlaylist(
          editingPlaylistForm.id,
          editingPlaylistForm.name,
          editingPlaylistForm.description
        );
        showEditModal.value = false; // 成功后关闭模态框
      } catch (error) {
        // 错误处理已在 store 中统一完成
      }
    } else {
      message.error('请填写完整的播放列表信息');
    }
  });
};

// --- 删除播放列表 ---
const handleDeletePlaylist = async (playlistId) => {
  try {
    await playlistStore.deletePlaylist(playlistId);
  } catch (error) {
    // 错误处理已在 store 中统一完成
  }
};

// --- 导航到播放列表详情页 ---
const goToPlaylistDetail = (playlistId) => {
  router.push(`/playlists/${playlistId}`);
};

// 组件挂载时自动获取播放列表
onMounted(() => {
  playlistStore.fetchMyPlaylists();
});
</script>

<style scoped>
.my-playlists-container {
  max-width: 960px;
  margin: 30px auto;
  padding: 20px;
}
</style>
