package com.melodymix.backend.controller;

import com.melodymix.backend.dto.CreatePlaylistRequest;
import com.melodymix.backend.dto.UpdatePlaylistRequest;
import com.melodymix.backend.model.Playlist;
import com.melodymix.backend.model.User;
import com.melodymix.backend.service.PlaylistService;
import jakarta.persistence.EntityNotFoundException; // 1. 新增导入
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 2. 新增导入 (推荐)
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // --- (createPlaylist, getMyPlaylists, addSongToPlaylist, deletePlaylist, updatePlaylist 方法保持不变) ---
    @PostMapping
    @PreAuthorize("isAuthenticated()") // 推荐为所有需要登录的操作添加此注解
    public ResponseEntity<Playlist> createPlaylist(@Valid @RequestBody CreatePlaylistRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        Playlist newPlaylist = playlistService.createPlaylist(request.getName(), request.getDescription(), currentUser);
        return new ResponseEntity<>(newPlaylist, HttpStatus.CREATED);
    }
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Playlist>> getMyPlaylists(@AuthenticationPrincipal User currentUser) {
        List<Playlist> playlists = playlistService.getPlaylistsByUser(currentUser);
        return ResponseEntity.ok(playlists);
    }
    @PostMapping("/{playlistId}/songs/{songId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Playlist> addSongToPlaylist(@PathVariable Long playlistId,
                                                      @PathVariable Long songId,
                                                      @AuthenticationPrincipal User currentUser) {
        Playlist updatedPlaylist = playlistService.addSongToPlaylist(playlistId, songId, currentUser);
        return ResponseEntity.ok(updatedPlaylist);
    }
    @DeleteMapping("/{playlistId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long playlistId,
                                               @AuthenticationPrincipal User currentUser) {
        playlistService.deletePlaylist(playlistId, currentUser);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{playlistId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable Long playlistId,
                                                   @Valid @RequestBody UpdatePlaylistRequest request,
                                                   @AuthenticationPrincipal User currentUser) {
        Playlist updatedPlaylist = playlistService.updatePlaylist(playlistId, request.getName(), request.getDescription(), currentUser);
        return ResponseEntity.ok(updatedPlaylist);
    }
    // --- (以上方法保持不变) ---

    // --- 3. 在类的末尾添加新的 API 端点 ---

    /**
     * 获取单个播放列表的详细信息
     * 任何登录用户都可以查看播放列表
     */
    @GetMapping("/{playlistId}")
    @PreAuthorize("isAuthenticated()") // 确保用户已登录才能查看
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable Long playlistId) {
        Playlist playlist = playlistService.getPlaylistById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("播放列表未找到，ID: " + playlistId));
        return ResponseEntity.ok(playlist);
    }

    /**
     * 从播放列表中移除一首歌曲
     * 只有播放列表所有者才能执行此操作
     */
    @DeleteMapping("/{playlistId}/songs/{songId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeSongFromPlaylist(@PathVariable Long playlistId,
                                                       @PathVariable Long songId,
                                                       @AuthenticationPrincipal User currentUser) {
        playlistService.removeSongFromPlaylist(playlistId, songId, currentUser);
        return ResponseEntity.noContent().build(); // 返回 204 No Content 表示成功
    }
}
