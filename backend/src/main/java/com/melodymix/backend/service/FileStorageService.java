package com.melodymix.backend.service;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

public interface FileStorageService {
    // ✅ 新增：通用的文件存储方法
    String storeFile(MultipartFile file); // 新增的通用方法，SongController 将调用它

    String storeSongFile(MultipartFile file); // 保持原有

    String storeCoverFile(MultipartFile file); // 保持原有

    boolean deleteFile(String fileUrl);

    Path getFileByUrl(String fileUrl);
}
