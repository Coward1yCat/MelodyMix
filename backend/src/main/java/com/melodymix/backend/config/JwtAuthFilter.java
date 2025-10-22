package com.melodymix.backend.config;

import com.melodymix.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // 声明为 Spring Bean，让 Spring 容器来管理它
@RequiredArgsConstructor // 自动生成包含 final 字段的构造函数
public class JwtAuthFilter extends OncePerRequestFilter { // 保证每次请求只执行一次过滤

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // 我们在 ApplicationConfig 中定义的 Bean

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头中获取 Authorization 字段
        final String authHeader = request.getHeader("Authorization");

        // 2. 如果 Authorization 字段为空，或者不以 "Bearer " 开头，直接放行
        //    这通常是访问登录、注册等公开接口的请求
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 放行请求
            return; // 结束方法
        }

        // 3. 提取 JWT (去掉 "Bearer " 前缀)
        final String jwt = authHeader.substring(7);

        // 4. 从 JWT 中解析出用户名
        final String username = jwtService.extractUsername(jwt);

        // 5. 检查用户名不为空，并且当前安全上下文中没有已认证的用户
        //    (SecurityContextHolder.getContext().getAuthentication() == null) 确保这只是在请求开始时设置一次认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. 根据用户名从数据库加载用户详细信息
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. 验证 Token 是否有效
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 8. 如果 Token 有效，创建一个 Spring Security 能理解的认证对象
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // JWT认证中我们不需要凭证（密码）
                        userDetails.getAuthorities()
                );

                // 9. 为认证对象设置请求的详细信息
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. 更新安全上下文，将当前用户标记为已认证
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 11. 将请求传递给过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}
