package com.melodymix.backend.config; // 确保包名与您现有文件一致

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类，用于全局配置 CORS (跨源资源共享) 策略。
 * 实现 Spring 的 WebMvcConfigurer 接口并重写 addCorsMappings 方法，
 * 以便在应用程序启动时自动应用 CORS 规则。
 */
@Configuration // 告诉 Spring 这是一个配置类，它包含用于配置应用程序行为的 Bean 定义或指令
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置全局 CORS 策略。
     * 这将允许前端应用程序 (例如在 http://localhost:5173 运行的 Vue.js 应用)
     * 能够向后端 (http://localhost:8080) 发送跨域请求。
     *
     * @param registry 用于注册 CORS 映射的 CorsRegistry 对象。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有 API 路径 (例如 /api/auth/**, /api/user/**, /api/songs/**, /api/v1/demo/**)
                // 允许从您的前端 Vue 应用的来源域名和端口发送请求。
                // 在生产环境中，这应该替换为您的生产前端域名，并且可以有多个。
                .allowedOrigins("http://localhost:5173") // ⬅️ 确保这里是您的 Vue 前端实际运行的地址和端口
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许前端发送的 HTTP 方法
                .allowedHeaders("*") // 允许前端发送所有类型的请求头 (包括 Authorization 头)
                .allowCredentials(true) // 允许前端携带认证信息 (如 JWT Token、Session Cookie 等)
                .maxAge(3600); // 设置预检请求 (OPTIONS 请求) 的缓存时间，单位为秒。
        // 在此时间内，浏览器不需要再次发送 OPTIONS 请求来检查 CORS 规则。
    }
}
