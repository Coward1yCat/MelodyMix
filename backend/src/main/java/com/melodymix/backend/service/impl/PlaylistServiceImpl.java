package com.melodymix.backend.service.impl;

import com.melodymix.backend.model.Playlist;
import com.melodymix.backend.model.Song;
import com.melodymix.backend.model.User;
import com.melodymix.backend.repository.PlaylistRepository;
import com.melodymix.backend.repository.SongRepository;
import com.melodymix.backend.service.PlaylistService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional; // 1. 新增导入

@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    // --- (createPlaylist, getPlaylistsByUser, addSongToPlaylist, deletePlaylist, updatePlaylist 方法保持不变) ---
    @Override
    @Transactional
    public Playlist createPlaylist(String name, String description, User owner) {
        Playlist playlist = Playlist.builder()
                .name(name)
                .description(description)
                .owner(owner)
                .build();
        return playlistRepository.save(playlist);
    }
    @Override
    public List<Playlist> getPlaylistsByUser(User user) {
        return playlistRepository.findByOwnerId(user.getId());
    }
    @Override
    @Transactional
    public Playlist addSongToPlaylist(Long playlistId, Long songId, User user) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("播放列表未找到，ID: " + playlistId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("歌曲未找到，ID: " + songId));
        if (!Objects.equals(playlist.getOwner().getId(), user.getId())) {
            throw new AccessDeniedException("您没有权限修改此播放列表");
        }
        playlist.getSongs().add(song);
        return playlistRepository.save(playlist);
    }
    @Override
    @Transactional
    public void deletePlaylist(Long playlistId, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("播放列表未找到，ID: " + playlistId));
        if (!Objects.equals(playlist.getOwner().getId(), currentUser.getId())) {
            throw new AccessDeniedException("您没有权限删除此播放列表");
        }
        playlistRepository.delete(playlist);
    }
    @Override
    @Transactional
    public Playlist updatePlaylist(Long playlistId, String newName, String newDescription, User currentUser) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("播放列表未找到，ID: " + playlistId));
        if (!Objects.equals(playlist.getOwner().getId(), currentUser.getId())) {
            throw new AccessDeniedException("您没有权限修改此播放列表");
        }
        playlist.setName(newName);
        playlist.setDescription(newDescription);
        return playlistRepository.save(playlist);
    }
    // --- (以上方法保持不变) ---

    // --- 2. 在类的末尾添加新方法的实现 ---

    @Override
    public Optional<Playlist> getPlaylistById(Long playlistId) {
        return playlistRepository.findById(playlistId);
    }

    @Override
    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId, User currentUser) {
        // 查找播放列表
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("播放列表未找到，ID: " + playlistId));

        // 验证用户权限，只有所有者才能移除歌曲
        if (!Objects.equals(playlist.getOwner().getId(), currentUser.getId())) {
            throw new AccessDeniedException("您没有权限修改此播放列表");
        }

        // 查找要移除的歌曲
        Song songToRemove = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("歌曲未找到，ID: " + songId));

        // 从播放列表的歌曲集合中移除歌曲
        boolean removed = playlist.getSongs().remove(songToRemove);

        // 如果歌曲不在播放列表中，可以抛出异常（可选，但友好）
        if (!removed) {
            throw new EntityNotFoundException("该歌曲不在播放列表中，无法移除。歌曲ID: " + songId);
        }

        // 保存更改，JPA会自动更新中间表
        playlistRepository.save(playlist);
    }
}
