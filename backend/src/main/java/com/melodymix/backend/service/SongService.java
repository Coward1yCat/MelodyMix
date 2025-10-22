package com.melodymix.backend.service;

import com.melodymix.backend.dto.CreateSongRequest;
import com.melodymix.backend.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SongService {

    Song createSong(CreateSongRequest request);

    // ✅ 新增：更新歌曲方法
    Song updateSong(Long songId, CreateSongRequest request);

    Page<Song> getAllSongs(Pageable pageable);

    Page<Song> searchSongsByTitle(String title, Pageable pageable);

    // ✅ 修正：deleteSong 方法的返回类型 (之前是Song，通常删除是返回void或被删除的实体用于确认)
    // 根据上下文，如果 Controller 需要处理文件删除，返回 Song 是合理的，但通常 void 即可
    // 如果需要返回被删除的歌曲信息以便 Controller 处理文件删除，则 Song 是合适的。
    void deleteSong(Long songId); // 通常删除返回 void

    // ✅ 新增：根据ID获取歌曲
    Song getSongById(Long songId);
}
