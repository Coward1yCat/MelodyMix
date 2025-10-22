package com.melodymix.backend.config;

import com.melodymix.backend.config.JwtAuthFilter; // ✅ 修正了导入路径，根据截图它位于 config 包内
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/uploads/songs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/covers/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/songs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/songs/{songId}").permitAll()

                        // 允许 ADMIN 和 COMPANY 角色执行文件上传接口 POST /api/songs/upload/file
                        .requestMatchers(HttpMethod.POST, "/api/songs/upload/file").hasAnyRole("ADMIN", "COMPANY")

                        // ✅ 移除旧的 POST /api/songs/add 规则，因为它已被 POST /api/songs 替代
                        // .requestMatchers(HttpMethod.POST, "/api/songs/add").hasAnyRole("ADMIN", "COMPANY")

                        // ✅ 新增：允许 ADMIN 和 COMPANY 角色执行歌曲添加的最终提交 (POST /api/songs)
                        .requestMatchers(HttpMethod.POST, "/api/songs").hasAnyRole("ADMIN", "COMPANY")

                        // 允许 ADMIN 和 COMPANY 角色执行歌曲更新 (PUT /api/songs/{songId})
                        .requestMatchers(HttpMethod.PUT, "/api/songs/{songId}")
                        .hasAnyRole("ADMIN", "COMPANY")
                        // 允许 ADMIN 和 COMPANY 角色执行歌曲删除 (DELETE /api/songs/{songId})
                        .requestMatchers(HttpMethod.DELETE, "/api/songs/{songId}")
                        .hasAnyRole("ADMIN", "COMPANY")

                        // 允许所有已认证用户访问其个人信息 (/api/user/me)
                        .requestMatchers(HttpMethod.GET, "/api/user/me").authenticated()

                        // ✅ 取消注释：允许所有已认证用户更新个人资料 PUT /api/user
                        // 【请确保您的 UserController 中有 /api/user 的 PUT 接口】
                        .requestMatchers(HttpMethod.PUT, "/api/user").authenticated()
                        // ✅ 取消注释：允许所有已认证用户修改密码 PUT /api/user/change-password
                        // 【请确保您的 UserController 中有 /api/user/change-password 的 PUT 接口】
                        .requestMatchers(HttpMethod.PUT, "/api/user/change-password").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                        .accessDeniedHandler(jwtAccessDeniedHandler())
                );
        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
            writer.flush();
        };
    }

    @Bean
    public AccessDeniedHandler jwtAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write("{\"error\": \"Forbidden\", \"message\": \"" + accessDeniedException.getMessage() + "\"}");
            writer.flush();
        };
    }
}
