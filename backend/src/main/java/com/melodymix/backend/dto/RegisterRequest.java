package com.melodymix.backend.dto;

import com.melodymix.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 注册请求 DTO
 * @Data: Lombok注解，生成getters, setters等。
 * @NotBlank: 验证注解，确保字符串不为空且不仅仅是空格。
 * @Email: 验证注解，确保字符串是合法的邮箱格式。
 * @NotNull: 验证注解，确保对象不为null。
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "角色不能为空")
    private Role role;

    // 公司用户字段，非必填
    private String companyName;
    private String companyAddress;
}
