package com.melodymix.backend.controller;

import com.melodymix.backend.dto.CreateSongRequest;
import com.melodymix.backend.model.Song;
import com.melodymix.backend.service.SongService;
import com.melodymix.backend.service.FileStorageService; // 确保导入 FileStorageService
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final FileStorageService fileStorageService;

    /**
     * 添加新歌曲（此接口用于接收所有元数据和已上传文件的URL）。
     * 前端 UploadView.vue 在分别上传歌曲文件和封面文件后，最终会调用这个接口提交歌曲信息。
     */
    @PostMapping // 对应 /api/songs
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')") // 允许 ADMIN 和 COMPANY 角色
    public ResponseEntity<Song> createSongWithUploadedFiles(@Valid @RequestBody CreateSongRequest request) {
        // CreateSongRequest 现在应该包含 title, artist, songUrl, coverUrl (以及其他元数据如 album, duration)
        Song createdSong = songService.createSong(request);
        return ResponseEntity.ok(createdSong);
    }

    /**
     * 更新现有歌曲信息。
     */
    @PutMapping("/{songId}") // 对应 /api/songs/{songId}
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')") // 允许 ADMIN 和 COMPANY 角色
    public ResponseEntity<Song> updateSong(@PathVariable Long songId, @Valid @RequestBody CreateSongRequest request) {
        Song updatedSong = songService.updateSong(songId, request); // <-- 编译错误修复：SongService 需定义此方法
        return ResponseEntity.ok(updatedSong);
    }

    /**
     * 删除指定歌曲。
     */
    @DeleteMapping("/{songId}") // 对应 /api/songs/{songId}
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')") // 权限修正：允许 ADMIN 和 COMPANY 角色
    public ResponseEntity<Void> deleteSong(@PathVariable Long songId) {
        songService.deleteSong(songId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 根据条件获取歌曲列表（支持分页和搜索）。
     * @param title (可选) 搜索关键词
     * @param pageable (自动注入) 分页参数 (e.g., ?page=0&size=10&sort=title,asc)
     * @return 分页后的歌曲数据
     */
    @GetMapping // 对应 /api/songs (或 /api/songs?title=xxx&page=x&size=y)
    public ResponseEntity<Page<Song>> getSongs(
            @RequestParam(required = false) String title,
            Pageable pageable) {

        Page<Song> songPage;

        if (title != null && !title.trim().isEmpty()) {
            songPage = songService.searchSongsByTitle(title, pageable);
        } else {
            songPage = songService.getAllSongs(pageable);
        }

        return ResponseEntity.ok(songPage);
    }

    /**
     * 上传单个文件（歌曲或封面）并返回其存储URL。
     * 此接口主要用于前端在提交歌曲元数据前，分别上传歌曲文件和封面文件。
     */
    @PostMapping("/upload/file") // 对应 /api/songs/upload/file
    // @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY')") // 安全性已由 SecurityConfig 统一管理
    public ResponseEntity<String> uploadFileAndReturnUrl(@RequestParam("file") MultipartFile multipartFile) {
        String fileUrl = fileStorageService.storeFile(multipartFile); // <-- 编译错误修复：FileStorageService 需定义此方法
        return ResponseEntity.ok(fileUrl);
    }

    // 提示：你原始代码中的 @PostMapping("/add") 接口已不再使用，被 @PostMapping ("/api/songs") 代替
    // 原有的 /search 接口和老的 getAllSongs 接口已被上面的 @GetMapping 代替
}
