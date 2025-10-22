package com.melodymix.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@Profile({"dev", "default"}) // 仅在开发和默认配置文件中激活
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-url}")
    private String baseUrl;

    @Value("${file.upload.dir.songs}")
    private String songUploadDir;

    @Value("${file.upload.dir.covers}")
    private String coverUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteSongUploadPath = Paths.get(songUploadDir).toAbsolutePath().normalize().toUri().toString();
        String absoluteCoverUploadPath = Paths.get(coverUploadDir).toAbsolutePath().normalize().toUri().toString();

        // 确保路径以斜杠结尾，这样才能正确映射目录下的所有文件
        if (!absoluteSongUploadPath.endsWith("/")) {
            absoluteSongUploadPath += "/";
        }
        if (!absoluteCoverUploadPath.endsWith("/")) {
            absoluteCoverUploadPath += "/";
        }

        registry.addResourceHandler(baseUrl + "/songs/**") // 例如 /uploads/songs/**
                .addResourceLocations(absoluteSongUploadPath);

        registry.addResourceHandler(baseUrl + "/covers/**") // 例如 /uploads/covers/**
                .addResourceLocations(absoluteCoverUploadPath);
    }
}
