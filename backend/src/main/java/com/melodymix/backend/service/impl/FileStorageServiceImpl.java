// src/main/java/com/melodymix/backend/service/impl/FileStorageServiceImpl.java

package com.melodymix.backend.service.impl;

import com.melodymix.backend.exception.FileOperationException;
import com.melodymix.backend.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects; // 导入 Objects
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final String songUploadDir;
    private final String coverUploadDir;
    private final String baseUrl;

    private Path songStorageLocation;
    private Path coverStorageLocation;
    private Path baseStorageLocation; // 新增一个基础存储位置

    public FileStorageServiceImpl(
            @Value("${file.upload.dir.songs}") String songUploadDir,
            @Value("${file.upload.dir.covers}") String coverUploadDir,
            @Value("${file.upload.base-url}") String baseUrl) {
        this.songUploadDir = songUploadDir;
        this.coverUploadDir = coverUploadDir;
        this.baseUrl = baseUrl;
    }

    @PostConstruct
    public void init() {
        try {
            this.songStorageLocation = Paths.get(this.songUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.songStorageLocation);

            this.coverStorageLocation = Paths.get(this.coverUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.coverStorageLocation);

            // 为了安全，基准路径应该高于所有实际存储目录，例如 `./uploads`
            // 我的假设是 songUploadDir 和 coverUploadDir 都在一个更顶级的 `uploads` 目录下
            // 例如 songUploadDir = ./uploads/songs, coverUploadDir = ./uploads/covers
            // 那么 baseStorageLocation 就是 './uploads'
            // if (this.songStorageLocation.getParent() != null) {
            // this.baseStorageLocation = this.songStorageLocation.getParent().toAbsolutePath().normalize();
            //} else {
            // Fallback for single-level directories
            this.baseStorageLocation = Paths.get("./uploads").toAbsolutePath().normalize();
            if(!Files.exists(this.baseStorageLocation)) Files.createDirectories(this.baseStorageLocation);
            // }


            System.out.println("歌曲上传目录: " + this.songStorageLocation);
            System.out.println("封面上传目录: " + this.coverStorageLocation);
            System.out.println("文件存储根目录 (用于安全检查): " + this.baseStorageLocation);

        } catch (IOException ex) {
            throw new FileOperationException("无法创建文件上传目录！", ex);
        }
    }

    /**
     * 通用的文件存储辅助方法。
     * @param file 要存储的 MultipartFile
     * @param storageLocation 目标存储路径 (Path)
     * @param subDirName URL 中表示子目录的名称 (例如 "songs", "covers")
     * @return 文件的完整访问 URL
     */
    private String storeInternalFile(MultipartFile file, Path storageLocation, String subDirName) { // ✅ 原有的 storeFile 改名为 storeInternalFile
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);

        String fileName = UUID.randomUUID().toString() + (fileExtension != null && !fileExtension.isEmpty() ? "." + fileExtension : "");

        try {
            if (fileName.contains("..")) {
                throw new FileOperationException("文件名包含无效路径序列 " + fileName);
            }

            Path targetLocation = storageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(baseUrl) // e.g., /uploads
                    .path("/")
                    .path(subDirName) // "songs" or "covers"
                    .path("/")
                    .path(fileName)
                    .toUriString();

        } catch (IOException ex) {
            throw new FileOperationException("无法存储文件 " + fileName + "，请重试！", ex);
        }
    }

    @Override
    public String storeSongFile(MultipartFile file) {
        return storeInternalFile(file, songStorageLocation, "songs");
    }

    @Override
    public String storeCoverFile(MultipartFile file) {
        return storeInternalFile(file, coverStorageLocation, "covers");
    }

    /**
     * ✅ 新增：实现 FileStorageService 接口中的通用 storeFile(MultipartFile file) 方法。
     * 此方法根据文件的 MIME 类型大致判断文件类型，并分发到合适的存储目录。
     * **注意：** 这种自动判断方式可能不够精确，实际项目中建议前端明确告知文件类型或目标目录。
     */
    @Override
    public String storeFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("audio/")) {
            return storeSongFile(file); // 歌曲文件
        } else if (contentType != null && contentType.startsWith("image/")) {
            return storeCoverFile(file); // 封面文件
        } else {
            // 更多默认处理或抛出不支持的类型异常
            throw new FileOperationException("不支持的文件类型: " + contentType + "。请上传音频或图片文件。");
        }
    }


    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }

        try {
            Path filePath = getFileByUrl(fileUrl);
            if (filePath != null && Files.exists(filePath) && filePath.startsWith(this.baseStorageLocation)) {
                return Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            System.err.println("删除文件时出错: " + fileUrl + ", 错误: " + e.getMessage());
        }
        return false;
    }

    /**
     * 将完整的 URL 解析为服务器上的文件系统路径。包含安全检查。
     */
    @Override
    public Path getFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();

            String expectedPrefix = this.baseUrl; // baseUrl 为 /uploads

            // 检查路径是否以我们的 /uploads 开头
            if (!path.startsWith(expectedPrefix + "/")) {
                // 如果路径是例如 /uploads/songs/abc.mp3，那么 path.startsWith("/uploads/") 应该为 true
                // 如果是 /api/files/songs/abc.mp3 这种形式，则需要调整 expectedPrefix 或判断逻辑
                // 假设 baseUrl 是 /uploads，实际存储的URL前缀是 /uploads/songs 或 /uploads/covers
                if (!path.startsWith("/uploads/songs/") && !path.startsWith("/uploads/covers/")) {
                    return null; // 不是我们服务管理的文件 URL
                }
            }

            // 提取 /uploads/ 之后的部分, 例如 "songs/uuid.mp3" 或 "covers/uuid.jpg"
            String relativePath = path.substring(expectedPrefix.length() + 1);

            // 将相对 Web 路径解析为服务器上的绝对文件系统路径
            Path resolvedPath = this.baseStorageLocation.resolve(relativePath).toAbsolutePath().normalize();

            // 再次确认解析后的路径仍然在我们的基础存储目录之下，防止路径遍历攻击
            if (resolvedPath.startsWith(this.baseStorageLocation)) {
                return resolvedPath;
            }

        } catch (Exception e) {
            System.err.println("解析文件URL失败: " + fileUrl + ", 错误: " + e.getMessage());
        }
        return null;
    }
}
