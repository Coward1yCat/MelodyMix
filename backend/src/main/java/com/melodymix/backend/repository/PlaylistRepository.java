package com.melodymix.backend.repository;

import com.melodymix.backend.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.melodymix.backend.model.Song;
import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    // 根据用户ID查找其所有播放列表
    List<Playlist> findByOwnerId(Long ownerId);
    List<Playlist> findBySongsContaining(Song song);
}
