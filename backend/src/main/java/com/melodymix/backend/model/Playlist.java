package com.melodymix.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // --- 关系定义 ---

    // 1. 与 User 的“多对一”关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // 外键列
    @JsonIgnore // 避免在返回播放列表时无限循环地返回用户信息
    private User owner;

    // 2. 与 Song 的“多对多”关系 (与User-Song的收藏关系类似)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    private Set<Song> songs = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}

