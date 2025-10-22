package com.melodymix.backend.controller;

import com.melodymix.backend.dto.UserDto;
import com.melodymix.backend.model.Song;
import com.melodymix.backend.model.User;
import com.melodymix.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/user") // [6]
@RequiredArgsConstructor      // [6]
public class UserController {

    private final UserService userService;

    // --- 已有代码 ---
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // 可选但推荐添加，保持一致性
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .companyName(user.getCompanyName())
                .companyAddress(user.getCompanyAddress())
                .build();
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // --- 修改部分：为以下三个API接口添加 @PreAuthorize 注解 ---

    /**
     * 将一首歌添加到当前用户的喜欢列表
     */
    @PostMapping("/likes/{songId}")
    @PreAuthorize("isAuthenticated()") // <-- 关键修改 1: 添加此行
    public ResponseEntity<Void> likeSong(@AuthenticationPrincipal User user, @PathVariable Long songId) {
        userService.likeSong(user.getId(), songId);
        return ResponseEntity.ok().build();
    }

    /**
     * 从当前用户的喜欢列表中移除一首歌
     */
    @DeleteMapping("/likes/{songId}")
    @PreAuthorize("isAuthenticated()") // <-- 关键修改 2: 添加此行
    public ResponseEntity<Void> unlikeSong(@AuthenticationPrincipal User user, @PathVariable Long songId) {
        userService.unlikeSong(user.getId(), songId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取当前用户所有喜欢的歌曲
     */
    @GetMapping("/likes")
    @PreAuthorize("isAuthenticated()") // <-- 关键修改 3: 添加此行
    public ResponseEntity<Set<Song>> getLikedSongs(@AuthenticationPrincipal User user) {
        Set<Song> likedSongs = userService.getLikedSongs(user.getId());
        return ResponseEntity.ok(likedSongs);
    }
}
