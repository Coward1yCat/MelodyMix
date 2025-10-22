// src/stores/playlist.js

import { defineStore } from 'pinia';
import axios from 'axios';
import { ref } from 'vue';

export const usePlaylistStore = defineStore('playlist', () => {
    const myPlaylists = ref([]);
    const playlistDetail = ref(null); // 用于存储单个播放列表的详细信息
    const isLoading = ref(false);

    // 获取用户的所有播放列表
    async function fetchMyPlaylists(force = false) {
        // 如果数据已存在且不是强制刷新，则直接返回
        if (myPlaylists.value.length > 0 && !force) {
            return;
        }

        isLoading.value = true;
        try {
            // 注意：这里确保路径是 "/api/playlists/my"
            const response = await axios.get('/playlists/my');
            myPlaylists.value = response.data;
        } catch (error) {
            console.error('获取我的播放列表失败:', error);
            // ✅ 这里是错误日志中指向的位置，但问题根源通常在axios全局配置或后端
            window.$message.error('无法加载您的播放列表');
            // 如果是 403 错误，可能需要特殊处理，例如跳转到登录页或显示权限不足提示
            if (error.response && error.response.status === 403) {
                window.$message.warning('您没有权限访问此内容，请检查您的登录状态或角色。');
            }
        } finally {
            isLoading.value = false;
        }
    }

    // 获取单个播放列表的详细信息
    async function fetchPlaylistDetail(playlistId) {
        isLoading.value = true;
        try {
            const response = await axios.get(`/playlists/${playlistId}`);
            playlistDetail.value = response.data;
            return response.data;
        } catch (error) {
            console.error('获取播放列表详情失败:', error);
            window.$message.error('无法加载播放列表详情');
            playlistDetail.value = null; // 获取失败时清空详情
            if (error.response && error.response.status === 403) {
                window.$message.warning('您没有权限访问此播放列表。');
            } else if (error.response && error.response.status === 404) {
                window.$message.warning('此播放列表不存在。');
            }
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // 添加歌曲到指定的播放列表
    async function addSongToPlaylist(playlistId, songId) {
        try {
            await axios.post(`/playlists/${playlistId}/songs/${songId}`);
            window.$message.success('成功添加到播放列表！');

            await fetchMyPlaylists(true); 
            if (playlistDetail.value && playlistDetail.value.id === playlistId) {
                await fetchPlaylistDetail(playlistId);
            }
            return true;
        } catch (error) {
            console.error('添加歌曲到播放列表失败:', error);
            const errorMsg = error.response?.data?.message || '添加失败，请重试';
            window.$message.error(errorMsg);
            return false;
        } finally {
            // 确保无论成功失败，加载状态都会被重置
            // 这里的 isLoading 是由 fetchMyPlaylists/fetchPlaylistDetail action管理, 不需要再次设置
        }
    }

    // 创建播放列表的 action
    async function createPlaylist(name, description) {
        isLoading.value = true;
        try {
            const response = await axios.post('/playlists', { name, description });
            window.$message.success('播放列表 "' + name + '" 创建成功！');
            await fetchMyPlaylists(true);
            return response.data;
        } catch (error) {
            console.error('创建播放列表失败:', error);
            const errorMsg = error.response?.data?.message || '创建失败，请重试';
            window.$message.error(errorMsg);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // 更新播放列表的 action
    async function updatePlaylist(playlistId, name, description) {
        isLoading.value = true;
        try {
            const response = await axios.put(`/playlists/${playlistId}`, { name, description });
            window.$message.success('播放列表 "' + name + '" 更新成功！');
            await fetchMyPlaylists(true);
            if (playlistDetail.value && playlistDetail.value.id === playlistId) {
                playlistDetail.value.name = name;
                playlistDetail.value.description = description;
            }
            return response.data;
        } catch (error) {
            console.error('更新播放列表失败:', error);
            const errorMsg = error.response?.data?.message || '更新失败，请重试';
            window.$message.error(errorMsg);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // 删除播放列表的 action
    async function deletePlaylist(playlistId) {
        isLoading.value = true;
        try {
            await axios.delete(`/playlists/${playlistId}`);
            window.$message.success('播放列表已删除。');
            await fetchMyPlaylists(true);
            if (playlistDetail.value && playlistDetail.value.id === playlistId) {
                playlistDetail.value = null;
            }
            return true;
        } catch (error) {
            console.error('删除播放列表失败:', error);
            const errorMsg = error.response?.data?.message || '删除失败，请重试';
            window.$message.error(errorMsg);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    // 从播放列表中移除歌曲
    async function removeSongFromPlaylist(playlistId, songId) {
        isLoading.value = true;
        try {
            await axios.delete(`/playlists/${playlistId}/songs/${songId}`);
            window.$message.success('歌曲已从播放列表中移除。');
            if (playlistDetail.value && playlistDetail.value.id === playlistId) {
                await fetchPlaylistDetail(playlistId);
            }
            await fetchMyPlaylists(true);
            return true;
        } catch (error) {
            console.error('从播放列表中移除歌曲失败:', error);
            const errorMsg = error.response?.data?.message || '移除失败，请重试';
            window.$message.error(errorMsg);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        myPlaylists,
        playlistDetail,
        isLoading,
        fetchMyPlaylists,
        fetchPlaylistDetail,
        addSongToPlaylist,
        createPlaylist,
        updatePlaylist,
        deletePlaylist,
        removeSongFromPlaylist
    };
});
