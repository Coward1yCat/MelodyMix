// src/stores/user.js

import { defineStore } from 'pinia';
import axios from 'axios';
import { ref, computed } from 'vue';

export const useUserStore = defineStore('user', () => {
    // ref() 就是 state
    const likedSongIds = ref(new Set()); // 使用 Set 存储喜欢的歌曲ID，性能更好

    // computed() 就是 getters
    const isSongLiked = computed(() => {
        return (songId) => likedSongIds.value.has(songId);
    });

    // function() 就是 actions
    async function fetchLikedSongs() {
        try {
            const response = await axios.get('/user/likes');
            const ids = response.data.map(song => song.id);
            likedSongIds.value = new Set(ids);
        } catch (error) {
            console.error('获取喜欢的歌曲列表失败:', error);
            window.$message.error('无法加载您喜欢的歌曲列表');
        }
    }

    async function likeSong(songId) {
        try {
            await axios.post(`/user/likes/${songId}`);
            likedSongIds.value.add(songId); // 立即更新UI状态
            window.$message.success('已添加到喜欢');
        } catch (error) {
            console.error('喜欢歌曲失败:', error);
            window.$message.error('操作失败，请重试');
        }
    }

    async function unlikeSong(songId) {
        try {
            await axios.delete(`/user/likes/${songId}`);
            likedSongIds.value.delete(songId); // 立即更新UI状态
            window.$message.success('已取消喜欢');
        } catch (error) {
            console.error('取消喜欢歌曲失败:', error);
            window.$message.error('操作失败，请重试');
        }
    }

    // 统一处理喜欢/取消喜欢的逻辑
    function toggleLike(songId) {
        if (isSongLiked.value(songId)) {
            unlikeSong(songId);
        } else {
            likeSong(songId);
        }
    }

    return { 
        likedSongIds, 
        isSongLiked, 
        fetchLikedSongs, 
        likeSong, 
        unlikeSong,
        toggleLike 
    };
});
