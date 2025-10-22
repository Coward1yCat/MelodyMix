<!-- src/components/AudioPlayer.vue -->
<template>
  <div v-if="playerStore.currentSong" class="audio-player-bar">
    <!-- 歌曲信息 -->
    <div class="song-info">
      <!-- ✅ 确保 playerStore.currentSong 是可选链操作，以防延迟加载 -->
      <img :src="playerStore.currentSong?.coverUrl || '/default-cover.png'" alt="Cover" class="song-cover">
      <div>
        <div class="song-title">{{ playerStore.currentSong?.title }}</div>
        <div class="song-artist">{{ playerStore.currentSong?.artist }}</div>
      </div>
    </div>

    <!-- 播放控制台 -->
    <div class="player-controls">
      <!-- 上一曲按钮 -->
      <n-button 
        text 
        circle 
        size="large" 
        @click="playerStore.playPrevious" 
        :disabled="!playerStore.hasPreviousSong"
      >
        <template #icon>
            <n-icon size="24">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M6 6L6 18L10.5 18L10.5 6L6 6ZM18 6L18 18L13.5 18L13.5 6L18 6Z" fill="currentColor"></path></svg>
            </n-icon>
        </template>
      </n-button>

      <!-- 播放/暂停按钮 -->
      <n-button 
        circle 
        size="large" 
        @click="playerStore.togglePlay" 
        :disabled="!playerStore.currentSong" 
        type="primary"
        style="margin: 0 10px;"
      >
          <template #icon>
              <n-icon size="28">
                  <svg v-if="playerStore.isPlaying" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M14 19V5H18V19H14ZM6 19V5H10V19H6Z" fill="currentColor"></path></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M8 5V19L19 12L8 5Z" fill="currentColor"></path></svg>
              </n-icon>
          </template>
      </n-button>
      
      <!-- 下一曲按钮 -->
      <n-button 
        text 
        circle 
        size="large" 
        @click="playerStore.playNext" 
        :disabled="!playerStore.hasNextSong"
      >
        <template #icon>
            <n-icon size="24">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M6 6L6 18L10.5 18L10.5 6L6 6ZM18 6L18 18L13.5 18L13.5 6L18 6Z" fill="currentColor"></path></svg>
            </n-icon>
        </template>
      </n-button>
    </div>

    <!-- 进度条和时间 -->
    <div class="progress-details">
        <span class="current-time">{{ formatTime(currentTime) }}</span>
        <input
            type="range"
            min="0"
            :max="duration"
            v-model="currentTime"
            class="progress-slider"
            @input="handleSeek"
            :disabled="!playerStore.currentSong"
            step="0.1"
        />
        <span class="total-time">{{ formatTime(duration) }}</span>
    </div>
    
    <!-- 音量控制 (新增) -->
    <div class="volume-control">
        <n-icon size="18" style="margin-right: 5px;">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M14 3.23V5.29C16.89 6.15 19 8.83 19 12C19 15.17 16.89 17.85 14 18.71V20.77C18.09 19.82 21 16.29 21 12C21 7.71 18.09 4.18 14 3.23ZM16.5 12C16.5 10.23 15.61 8.63 14 7.72V16.28C15.61 15.37 16.5 13.77 16.5 12ZM3 9V15H7L12 20V4L7 9H3Z" fill="currentColor"></path></svg>
        </n-icon>
        <input
            type="range"
            min="0"
            max="1"
            step="0.01"
            v-model="volume"
            class="volume-slider"
            @input="handleVolumeChange"
        />
    </div>

    <!-- 音频元素 (隐藏) -->
    <!-- ✅ fileUrl 应该是歌曲的播放链接 -->
    <audio ref="audioElement" :src="playerStore.currentSong?.fileUrl"></audio> 
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import { usePlayerStore } from '@/stores/player.js';
import { NButton, NIcon, useMessage } from 'naive-ui'; // ✅ 导入 NButton, NIcon, useMessage
import { nextTick } from 'vue'; // 明确导入 nextTick

const playerStore = usePlayerStore();
const message = useMessage(); // ✅ 实例化消息提示
const audioElement = ref(null); // 用于引用 <audio> 标签

const currentTime = ref(0); // 当前播放时间
const duration = ref(0);    // 歌曲总时长
const volume = ref(0.7);    // 默认音量 70%

// 格式化时间 (秒 -> 分:秒)
function formatTime(seconds) {
  if (typeof seconds !== 'number' || isNaN(seconds) || seconds < 0) return '00:00';
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = Math.floor(seconds % 60);
  return `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`;
}

// --- 音频事件监听器 ---
const setupAudioListeners = () => {
    if (!audioElement.value) return;

    // 歌曲加载完成或元数据加载完成时触发
    audioElement.value.onloadedmetadata = () => {
        duration.value = audioElement.value.duration;
        currentTime.value = 0; // 重置当前播放时间
    };

    // 播放时间更新时触发
    audioElement.value.ontimeupdate = () => {
        currentTime.value = audioElement.value.currentTime;
    };

    // 播放结束时触发
    audioElement.value.onended = () => {
        playerStore.pause(); // 暂停播放
        currentTime.value = 0; // 重置时间
        // 尝试播放下一首
        if (playerStore.hasNextSong) {
            playerStore.playNext();
        } else {
            message.info('播放列表已结束。');
        }
    };

    // 捕获播放错误
    audioElement.value.onerror = (e) => {
        console.error("音频播放错误:", e);
        playerStore.pause();
        message.error('歌曲加载或播放失败，请检查网络或歌曲文件。');
    };

    // 初始化音量
    audioElement.value.volume = volume.value;
};

// --- 监听器 ---

// 监听 playerStore 中 isPlaying 状态的变化
watch(() => playerStore.isPlaying, (newIsPlaying) => {
  if (!audioElement.value || !playerStore.currentSong) return;

  if (newIsPlaying) {
    audioElement.value.play().catch(error => {
        console.error("播放失败:", error);
        playerStore.pause(); // 如果自动播放失败，同步状态
        message.error('歌曲播放失败，请检查网络或歌曲文件。');
    });
  } else {
    audioElement.value.pause();
  }
});

// 监听当前歌曲的变化
watch(() => playerStore.currentSong, (newSong) => {
    if (newSong) {
        // 当歌曲改变时，audio 标签的 src 会自动更新。
        // 确保在 Vue 更新 DOM 后再调用 play()
        playerStore.isPlaying = true; // 自动开始播放新歌
        nextTick(() => { // ✅ 使用明确导入的 nextTick
            // 重新设置事件监听器（因为audio元素可能被替换，或者为了刷新引用）
            setupAudioListeners(); 
            audioElement.value?.load(); // 重新加载音频，确保新src生效
            if (playerStore.isPlaying) {
                audioElement.value?.play().catch(error => {
                    console.error("新歌播放失败:", error);
                    playerStore.pause();
                    message.error('新歌加载失败，请重试。');
                });
            }
        });
    } else {
        // 没有歌曲播放时，重置所有状态
        playerStore.pause();
        currentTime.value = 0;
        duration.value = 0;
        if(audioElement.value) audioElement.value.src = ''; // 清空 src
    }
}, { deep: true }); 

// --- 用户交互事件 ---

// 用户拖动进度条
const handleSeek = (event) => {
    const seekTime = parseFloat(event.target.value);
    if (audioElement.value) {
        audioElement.value.currentTime = seekTime;
        currentTime.value = seekTime; // 立即更新 UI
    }
};

// 用户改变音量
const handleVolumeChange = (event) => {
    const newVolume = parseFloat(event.target.value);
    if (audioElement.value) {
        audioElement.value.volume = newVolume;
    }
    volume.value = newVolume; // 更新 UI 状态
};

// --- 生命周期钩子 ---
onMounted(() => {
    setupAudioListeners(); // 挂载时设置监听器

    // 如果应用程序重新加载，并且之前有歌曲正在播放，尝试恢复音量和播放状态
    if (playerStore.hasCurrentSong && playerStore.isPlaying) {
        // nextTick 可以确保DOM已更新并audioElement可用
        nextTick(() => {
             audioElement.value.volume = volume.value;
             audioElement.value.play().catch(error => {
                 console.error("恢复播放失败:", error);
                 playerStore.pause();
             });
        });
    }
});

onBeforeUnmount(() => {
    // 清除所有事件监听器，避免内存泄漏
    if (audioElement.value) {
        audioElement.value.onloadedmetadata = null;
        audioElement.value.ontimeupdate = null;
        audioElement.value.onended = null;
        audioElement.value.onerror = null;
    }
});
</script>

<style scoped>
.audio-player-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 72px;
  background-color: #202020; /* 深色背景，如果和naive-ui主题有冲突可调整 */
  color: white;
  display: flex; /* 改用flex布局 */
  align-items: center;
  justify-content: space-between; /* 调整子元素间的间距 */
  padding: 0 20px;
  box-sizing: border-box;
  border-top: 1px solid #303030;
  z-index: 1000; /* 确保播放器在最上层 */
}

.song-info {
    display: flex;
    align-items: center;
    flex: 0 0 auto; /* 不允许缩小 */
    width: 20%; /* 固定宽度，防止溢出 */
    overflow: hidden; /* 隐藏溢出内容 */
}
.song-cover {
    width: 56px;
    height: 56px;
    margin-right: 15px;
    object-fit: cover;
    border-radius: 4px; /* 封面圆角，更美观 */
    flex-shrink: 0; /* 防止图像缩小 */
}
.song-title {
    font-weight: bold;
    white-space: nowrap; /* 不换行 */
    overflow: hidden;
    text-overflow: ellipsis; /* 文本溢出省略号 */
    flex-grow: 1;
}
.song-artist {
    font-size: 0.8em;
    color: #b3b3b3;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex-grow: 1;
}

.player-controls {
    flex: 0 0 auto;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 20%; /* 适当宽度 */
}

/* 歌曲进度 */
.progress-details {
    flex: 1; /* 占据剩余可用空间 */
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 0 20px;
}
.current-time, .total-time {
    font-size: 0.8em;
    color: #b3b3b3;
    min-width: 35px; /* 保证时间显示宽度 */
    text-align: center;
}
.progress-slider {
    flex: 1; /* 占据最大空间 */
    height: 4px;
    -webkit-appearance: none;
    appearance: none;
    background: #535353;
    cursor: pointer;
    border-radius: 2px;
    outline: none;
}
.progress-slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 12px;
    height: 12px;
    background: white;
    border-radius: 50%;
    transition: background .2s ease-in-out;
}
.progress-slider::-moz-range-thumb {
    width: 12px;
    height: 12px;
    background: white;
    border-radius: 50%;
    transition: background .2s ease-in-out;
}
.progress-slider:hover::-webkit-slider-thumb {
    background: rgb(24, 160, 88); /* 配合 Naive UI 的主色 */
}
.progress-slider:hover::-moz-range-thumb {
    background: rgb(24, 160, 88);
}

/* 音量控制 */
.volume-control {
    display: flex;
    align-items: center;
    flex: 0 0 auto;
    width: 120px; /* 音量控制的宽度 */
    margin-left: 20px;
}
.volume-slider {
    flex: 1;
    height: 4px;
    -webkit-appearance: none;
    appearance: none;
    background: #535353;
    cursor: pointer;
    border-radius: 2px;
    outline: none;
}
.volume-slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 10px;
    height: 10px;
    background: white;
    border-radius: 50%;
    transition: background .2s ease-in-out;
}
.volume-slider:hover::-webkit-slider-thumb {
    background: rgb(24, 160, 88);
}
</style>
