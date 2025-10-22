package com.melodymix.backend.config;

import com.melodymix.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * 定义 UserDetailsService Bean
     * Spring Security 将使用这个 Bean 来加载用户数据。
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到用户: " + username));
    }

    /**
     * 定义 AuthenticationProvider Bean
     * 这是数据访问认证提供者，它会使用 UserDetailsService 和 PasswordEncoder 来验证用户。
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService()); // 设置用户服务
        authProvider.setPasswordEncoder(passwordEncoder()); // 设置密码编码器
        return authProvider;
    }

    /**
     * 定义 AuthenticationManager Bean
     * 这是认证管理器，我们在登录时会用到它。
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 定义 PasswordEncoder Bean
     * 我们使用 BCrypt 算法进行密码加密。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
