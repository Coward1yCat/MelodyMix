package com.melodymix.backend.service;

import com.melodymix.backend.dto.UserDto;
import com.melodymix.backend.model.Song;
import java.util.List;
import java.util.Set;

public interface UserService {
    List<UserDto> getAllUsers();
    void likeSong(Long userId, Long songId);
    void unlikeSong(Long userId, Long songId);
    Set<Song> getLikedSongs(Long userId);
}
