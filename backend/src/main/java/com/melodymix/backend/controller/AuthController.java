package com.melodymix.backend.controller;

import com.melodymix.backend.dto.AuthenticationResponse; // 导入
import com.melodymix.backend.dto.LoginRequest; // 导入
import com.melodymix.backend.dto.RegisterRequest;
import com.melodymix.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; // 导入 Lombok
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // 使用 Lombok 自动生成构造函数
public class AuthController {

    private final AuthService authService;

    /**
     * 注册端点
     * 成功时返回 200 OK 和包含 JWT 的 JSON
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        // 直接调用服务并返回结果，代码更简洁
        // 如果 service 抛出异常, Spring 的默认异常处理器会处理 (后续可以自定义)
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * 新增：登录端点
     * 成功时返回 200 OK 和包含 JWT 的 JSON
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}
