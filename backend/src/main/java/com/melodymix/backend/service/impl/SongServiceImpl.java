package com.melodymix.backend.service.impl;

import com.melodymix.backend.dto.CreateSongRequest;
import com.melodymix.backend.model.Playlist;
import com.melodymix.backend.model.Song;
import com.melodymix.backend.repository.PlaylistRepository;
import com.melodymix.backend.repository.SongRepository;
import com.melodymix.backend.service.FileStorageService; // ✅ 引入 FileStorageService 来处理文件删除
import com.melodymix.backend.service.SongService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository; // 在 deleteSong 中用到
    private final FileStorageService fileStorageService; // ✅ 注入 FileStorageService

    @Override
    @Transactional
    public Song createSong(CreateSongRequest request) {
        Song newSong = Song.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .fileUrl(request.getFileUrl()) // ✅ 修正：将 getSongUrl() 改为 getFileUrl()
                .coverUrl(request.getCoverUrl())
                .build();
        return songRepository.save(newSong);
    }

    @Override
    @Transactional
    public Song updateSong(Long songId, CreateSongRequest request) {
        Song existingSong = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("无法更新：未找到ID为 " + songId + " 的歌曲"));

        existingSong.setTitle(request.getTitle());
        existingSong.setArtist(request.getArtist());
        existingSong.setAlbum(request.getAlbum());
        existingSong.setReleaseDate(request.getReleaseDate());
        existingSong.setDuration(request.getDuration());
        // 歌曲和封面 URL 通常不通过 update 接口直接修改。
        // 如果需要修改，应有单独的接口或特殊逻辑处理文件替换。
        // existingSong.setFileUrl(request.getFileUrl()); // ✅ 如果更新文件URL，应该用getFileUrl()
        // existingSong.setCoverUrl(request.getCoverUrl());

        return songRepository.save(existingSong);
    }

    @Override
    public Page<Song> getAllSongs(Pageable pageable) {
        return songRepository.findAll(pageable);
    }

    @Override
    public Page<Song> searchSongsByTitle(String title, Pageable pageable) {
        return songRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Override
    @Transactional
    public void deleteSong(Long songId) {
        Song songToDelete = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("无法删除：未找到ID为 " + songId + " 的歌曲"));

        // 从所有关联的播放列表中移除此歌曲
        // ✅ 修正：假设 PlaylistRepository 有 findBySongsContaining 方法
        // 如果没有，你需要根据实际的 PlaylistRepository 结构来获取包含此歌曲的播放列表
        List<Playlist> playlists = playlistRepository.findBySongsContaining(songToDelete);
        for (Playlist playlist : playlists) {
            playlist.getSongs().remove(songToDelete);
            playlistRepository.save(playlist); // 保存更新后的播放列表
        }

        // 删除歌曲记录
        songRepository.delete(songToDelete);

        // ✅ 新增：删除关联的文件 (歌曲文件和封面文件)
        if (songToDelete.getFileUrl() != null && !songToDelete.getFileUrl().isEmpty()) {
            fileStorageService.deleteFile(songToDelete.getFileUrl());
        }
        if (songToDelete.getCoverUrl() != null && !songToDelete.getCoverUrl().isEmpty()) {
            fileStorageService.deleteFile(songToDelete.getCoverUrl());
        }
    }

    @Override
    public Song getSongById(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("未找到ID为 " + songId + " 的歌曲"));
    }
}
