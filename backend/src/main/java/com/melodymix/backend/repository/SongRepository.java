package com.melodymix.backend.repository;

import com.melodymix.backend.model.Playlist;
import com.melodymix.backend.model.Song;
import org.springframework.data.domain.Page;         // 1. 新增导入
import org.springframework.data.domain.Pageable;      // 1. 新增导入
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // 2. 修改方法签名以支持分页
    // - 返回类型从 List<Song> 改为 Page<Song>
    // - 添加 Pageable pageable 参数
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // JpaRepository 已经自带了 findAll(Pageable pageable) 方法，我们无需声明它

    // 这个方法与分页无关，保持不变
    @Query("SELECT p FROM Playlist p JOIN p.songs s WHERE s.id = :songId")
    List<Playlist> findPlaylistsBySongId(Long songId);
}
