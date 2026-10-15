package com.agrocycle.agrocycle.service;

import com.agrocycle.agrocycle.exception.ApiException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${agrocycle.upload.dir:uploads}")
    private String uploadDir;

    @Value("${agrocycle.upload.max-size-bytes:10485760}")
    private long maxSizeBytes;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException("File is empty", 400);
        if (file.getSize() > maxSizeBytes) throw new ApiException("File exceeds maximum size of 10MB", 400);
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ApiException("Invalid file type. Allowed: JPEG, PNG, WebP, GIF", 400);
        }
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (ext == null || ext.isEmpty()) ext = "jpg";
        ext = ext.toLowerCase().replaceAll("[^a-z0-9]", "");
        String fileName = UUID.randomUUID().toString() + "." + ext;
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(fileName);
            if (!targetPath.startsWith(uploadPath)) throw new ApiException("Invalid file path", 400);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new ApiException("Could not store file: " + e.getMessage(), 500);
        }
    }

    public void delete(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", fileName);
        }
    }
}
