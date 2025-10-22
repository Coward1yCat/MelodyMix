package com.melodymix.backend.service;

import com.melodymix.backend.dto.AuthenticationResponse;
import com.melodymix.backend.dto.LoginRequest;
import com.melodymix.backend.dto.RegisterRequest;
import com.melodymix.backend.model.User;
import com.melodymix.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor; // 导入 Lombok
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// 使用 @RequiredArgsConstructor, Lombok 会自动为所有 final 字段生成构造函数
// 这比手动编写构造函数更简洁、不易出错
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // 1. 注入 JwtService
    private final AuthenticationManager authenticationManager; // 2. 注入 AuthenticationManager

    /**
     * 注册新用户并返回 JWT
     * @param request 包含用户注册信息的 DTO
     * @return 包含JWT的响应对象
     */
    public AuthenticationResponse register(RegisterRequest request) {
        // 检查用户名和邮箱是否已存在 (保留你原有的逻辑)
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("错误：该用户名已被注册！");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("错误：该邮箱已被注册！");
        }

        // 创建 User 实体对象
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .companyName(request.getCompanyName())
                .companyAddress(request.getCompanyAddress())
                .build();

        // 将新用户保存到数据库
        userRepository.save(user);

        // 3. 为新注册的用户生成一个 JWT
        var jwtToken = jwtService.generateToken(user);

        // 4. 返回包含 JWT 的响应
        return AuthenticationResponse.builder() 
                .token(jwtToken)
                .build();
    }

    /**
     * 5. 新增：用户登录方法
     * @param request 包含用户名和密码的登录请求
     * @return 包含JWT的认证响应
     */
    public AuthenticationResponse login(LoginRequest request) {
        // 5.1 执行认证
        // authenticationManager 会使用我们配置好的 UserDetailsService 和 PasswordEncoder 进行验证。
        // 如果密码或用户名错误，它会在这里抛出异常，后续代码不会执行。
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 5.2 如果认证成功，从数据库中查找用户
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("认证通过但未找到用户，这是一个严重错误"));

        // 5.3 为该用户生成一个新的 JWT
        var jwtToken = jwtService.generateToken(user);

        // 5.4 构建并返回响应
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
