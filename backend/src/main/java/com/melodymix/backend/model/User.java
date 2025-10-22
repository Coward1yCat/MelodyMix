package com.melodymix.backend.model;

import jakarta.persistence.*;
// --- 1. 新增导入 ---
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
// --- 已有导入 ---
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.ArrayList; // 1. 新增导入

// --- 2. 修改注解：将 @Data 替换为 @Getter 和 @Setter ---
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    // --- id, username, password 等字段保持不变 ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String companyName;
    private String companyAddress;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // --- 3. 在关联字段上添加排除注解 ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_liked_songs",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Song> likedSongs = new HashSet<>();


    // --- UserDetails 接口的实现方法保持不变 ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    // --- 2. 在类的末尾添加以下代码 ---
    // “一对多”关系：一个用户可以有多个播放列表
    // mappedBy = "owner" 指明这个关系由 Playlist 实体中的 "owner" 字段来维护
    // cascade = CascadeType.ALL 表示对用户的操作（如删除）会级联到其拥有的播放列表
    // orphanRemoval = true 表示当一个播放列表从这个集合中移除时，它也应该从数据库中删除
    @OneToMany(
            mappedBy = "owner",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Playlist> playlists = new ArrayList<>();
}
