<!-- src/views/UploadView.vue -->
<template>
  <div class="upload-container">
    <h2>上传新歌曲</h2>
    <form @submit.prevent="handleSubmit" class="upload-form">
      <!-- 标题输入 -->
      <div class="form-group">
        <label for="title">歌曲标题</label>
        <input id="title" v-model="songData.title" type="text" placeholder="输入歌曲名称" required />
      </div>

      <!-- 艺术家输入 -->
      <div class="form-group">
        <label for="artist">艺术家</label>
        <input id="artist" v-model="songData.artist" type="text" placeholder="输入艺术家名称" required />
      </div>

      <!-- 歌曲文件选择 -->
      <div class="form-group">
        <label for="songFile">歌曲文件 (MP3, WAV, etc.)</label>
        <!-- @change="handleFileSelect" 会在用户选择文件后立即触发 -->
        <input id="songFile" type="file" @change="handleFileSelect($event, 'song')" accept="audio/*" required />
      </div>

      <!-- 封面图片选择 -->
      <div class="form-group">
        <label for="coverFile">封面图片 (JPG, PNG, etc.)</label>
        <input id="coverFile" type="file" @change="handleFileSelect($event, 'cover')" accept="image/*" required />
      </div>
      
      <!-- 提交按钮和状态显示 -->
      <div class="form-actions">
        <button type="submit" :disabled="isLoading">
          {{ isLoading ? '正在上传...' : '确认上传' }}
        </button>
      </div>

      <!-- 错误和成功消息提示 -->
      <div v-if="errorMessage" class="message error-message">
        {{ errorMessage }}
      </div>
      <div v-if="successMessage" class="message success-message">
        {{ successMessage }}
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth'; // 假设你使用 Pinia 管理认证状态
import axios from 'axios'; // 或者你封装的 API 服务

// --- 响应式状态定义 ---
const songData = ref({
  title: '',
  artist: '',
});

const files = ref({
  song: null,
  cover: null,
});

const isLoading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const authStore = useAuthStore();

// --- 方法定义 ---

// 当用户选择文件时，将其存储在 files ref 中
function handleFileSelect(event, fileType) {
  const selectedFile = event.target.files[0];
  if (selectedFile) {
    files.value[fileType] = selectedFile;
  }
}

// 核心上传逻辑
async function handleSubmit() {
  // 0. 重置状态
  isLoading.value = true;
  errorMessage.value = '';
  successMessage.value = '';

  // 1. 基础验证
  if (!songData.value.title || !songData.value.artist || !files.value.song || !files.value.cover) {
    errorMessage.value = '请填写所有字段并选择两个文件。';
    isLoading.value = false;
    return;
  }

  try {
    // 2. 上传歌曲文件
    const songUrl = await uploadFile(files.value.song);
    if (!songUrl) throw new Error('歌曲文件上传失败。');

    // 3. 上传封面文件
    const coverUrl = await uploadFile(files.value.cover);
    if (!coverUrl) throw new Error('封面文件上传失败。');
    
    // 4. 所有文件上传成功后，提交歌曲元数据
    const createSongPayload = {
      title: songData.value.title,
      artist: songData.value.artist,
      songUrl: songUrl,
      coverUrl: coverUrl,
      // 可以在这里添加其他字段，如 album, duration 等
    };
    
    // 从你的 auth store 获取 token
    const token = authStore.token; 
    
    await axios.post('/api/songs', createSongPayload, { // 注意：根据你的Controller，这里可能是 /api/songs/add
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    // 5. 显示成功信息并重置表单
    successMessage.value = `歌曲 "${songData.value.title}" 上传成功！`;
    resetForm();

  } catch (error) {
    console.error('上传过程中发生错误:', error);
    errorMessage.value = error.response?.data?.message || error.message || '发生未知错误，请稍后重试。';
  } finally {
    isLoading.value = false;
  }
}

// 封装的可重用文件上传函数
async function uploadFile(file) {
  const formData = new FormData();
  formData.append('file', file);

  const token = authStore.token;

  try {
    const response = await axios.post('/api/songs/upload/file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    });
    // 后端返回的是文件的完整 URL
    return response.data; 
  } catch (error) {
    console.error('文件上传失败:', error);
    // 返回 null 或抛出错误，让调用者知道失败了
    return null; 
  }
}

function resetForm() {
  songData.value.title = '';
  songData.value.artist = '';
  files.value.song = null;
  files.value.cover = null;
  // 清除文件输入框的值
  document.getElementById('songFile').value = '';
  document.getElementById('coverFile').value = '';
}
</script>

<style scoped>
.upload-container {
  max-width: 600px;
  margin: 2rem auto;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}
.form-group {
  margin-bottom: 1.5rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.form-actions {
  text-align: center;
}
button {
  padding: 0.75rem 2rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s;
}
button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}
button:hover:not(:disabled) {
  background-color: #0056b3;
}
.message {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: 4px;
  text-align: center;
}
.error-message {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
.success-message {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}
</style>
