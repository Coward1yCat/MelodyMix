package com.melodymix.backend.model;

/**
 * 用户角色枚举
 * 定义了系统中存在的所有角色类型。
 * 使用枚举可以确保类型安全，避免因字符串拼写错误导致的问题。
 */
public enum Role {
    USER,    // 普通用户
    ADMIN,   // 管理员
    COMPANY  // 公司
}
