package com.melodymix.backend.service.impl;
import com.melodymix.backend.dto.UserDto;
import com.melodymix.backend.model.Song;
import com.melodymix.backend.model.User;
import com.melodymix.backend.repository.SongRepository;
import com.melodymix.backend.repository.UserRepository;
import com.melodymix.backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        // 我们必须将 User 实体列表映射到 UserDto 对象列表
        // 以避免暴露敏感数据。
        return users.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .companyName(user.getCompanyName())
                        .companyAddress(user.getCompanyAddress())
                        .build())
                .collect(Collectors.toList());
    }
    @Override
    @Transactional // 建议将修改操作放在事务中
    public void likeSong(Long userId, Long songId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户未找到，ID: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("歌曲未找到，ID: " + songId));
        user.getLikedSongs().add(song);
        userRepository.save(user); // JPA会自动处理中间表的插入操作 [5]
    }
    @Override
    @Transactional
    public void unlikeSong(Long userId, Long songId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户未找到，ID: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new EntityNotFoundException("歌曲未找到，ID: " + songId));
        user.getLikedSongs().remove(song);
        userRepository.save(user); // JPA会自动处理中间表的删除操作 [5]
    }
    @Override
    public Set<Song> getLikedSongs(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户未找到，ID: " + userId));
        return user.getLikedSongs();
    }
}
