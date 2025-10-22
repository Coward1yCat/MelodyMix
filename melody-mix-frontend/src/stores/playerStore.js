// src/stores/player.js

import { defineStore } from 'pinia';
import { ref } from 'vue';

export const usePlayerStore = defineStore('player', () => {
    // --- State ---
    const currentSong = ref(null); // 当前播放的歌曲对象
    const isPlaying = ref(false);  // 是否正在播放
    const queue = ref([]);         // 播放队列（可选，为未来做准备）

    // --- Actions ---

    // 播放一首新歌曲
    function playSong(song) {
        currentSong.value = song;
        isPlaying.value = true; 
        // 也可以在这里处理播放列表逻辑，例如将歌曲添加到队列
        // queue.value.push(song);
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

    // --- Getters (可选，但推荐) ---
    const hasCurrentSong = () => {
        return currentSong.value !== null;
    }

    return {
        currentSong,
        isPlaying,
        queue,
        playSong,
        togglePlay,
        pause,
        resume,
        hasCurrentSong
    };
});
