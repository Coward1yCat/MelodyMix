package com.melodymix.backend.service;

import com.melodymix.backend.model.Playlist;
import com.melodymix.backend.model.User;

import java.util.List;
import java.util.Optional; // 1. 新增导入

public interface PlaylistService {
    // 创建播放列表
    Playlist createPlaylist(String name, String description, User owner);

    // 获取用户的所有播放列表
    List<Playlist> getPlaylistsByUser(User user);

    // 向播放列表添加歌曲
    Playlist addSongToPlaylist(Long playlistId, Long songId, User user);

    // 删除播放列表
    void deletePlaylist(Long playlistId, User currentUser);

    // 更新播放列表
    Playlist updatePlaylist(Long playlistId, String newName, String newDescription, User currentUser);

    // --- 2. 新增方法声明 ---
    /**
     * 根据ID获取播放列表
     * @param playlistId 播放列表的ID
     * @return 返回一个包含播放列表的 Optional，如果未找到则为空
     */
    Optional<Playlist> getPlaylistById(Long playlistId);

    /**
     * 从播放列表中移除一首歌曲
     * @param playlistId 播放列表的ID
     * @param songId 要移除的歌曲ID
     * @param currentUser 当前操作的用户，用于权限验证
     */
    void removeSongFromPlaylist(Long playlistId, Long songId, User currentUser);
}
