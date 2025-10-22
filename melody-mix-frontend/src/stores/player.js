// src/stores/player.js

import { defineStore } from 'pinia';
import { ref, computed } from 'vue'; // ✅ 导入 computed

export const usePlayerStore = defineStore('player', () => {
    // --- State ---
    const currentSong = ref(null);      // 当前播放的歌曲对象
    const isPlaying = ref(false);       // 是否正在播放
    const queue = ref([]);              // 播放队列
    const currentQueueIndex = ref(-1);  // 当前播放歌曲在队列中的索引

    // --- Getters ---
    const hasCurrentSong = computed(() => currentSong.value !== null);
    const hasNextSong = computed(() => currentQueueIndex.value < queue.value.length - 1);
    const hasPreviousSong = computed(() => currentQueueIndex.value > 0);

    // --- Actions ---

    // 播放一首新歌曲 (会清空或替换当前队列)
    function playSong(song) {
        queue.value = [song];
        currentQueueIndex.value = 0;
        currentSong.value = song;
        isPlaying.value = true;
    }

    // 播放一个播放列表
    function playPlaylist(songsList) {
        if (!songsList || songsList.length === 0) {
            console.warn('播放列表为空，无法播放。');
            return;
        }
        queue.value = Array.isArray(songsList) ? songsList : [songsList]; // 确保是数组
        currentQueueIndex.value = 0;
        currentSong.value = queue.value[0];
        isPlaying.value = true;
    }

    // 播放队列中的下一首歌曲
    function playNext() {
        if (hasNextSong.value) {
            currentQueueIndex.value++;
            currentSong.value = queue.value[currentQueueIndex.value];
            isPlaying.value = true;
        } else {
            // 队列结束，可以停止播放或循环播放
            isPlaying.value = false;
            currentSong.value = null; // 清空当前歌曲
            currentQueueIndex.value = -1;
        }
    }

    // 播放队列中的上一首歌曲
    function playPrevious() {
        if (hasPreviousSong.value) {
            currentQueueIndex.value--;
            currentSong.value = queue.value[currentQueueIndex.value];
            isPlaying.value = true;
        }
    }

    // 切换播放/暂停状态
    function togglePlay() {
        if (currentSong.value) {
            isPlaying.value = !isPlaying.value;
        }
    }

    // 暂停播放
    function pause() {
        isPlaying.value = false;
    }

    // 继续播放
    function resume() {
        if (currentSong.value) {
            isPlaying.value = true;
        }
    }

    return {
        currentSong,
        isPlaying,
        queue,
        currentQueueIndex,
        hasCurrentSong,
        hasNextSong,
        hasPreviousSong,
        playSong,
        playPlaylist, 
        playNext,     
        playPrevious, 
        togglePlay,
        pause,
        resume,
    };
});
