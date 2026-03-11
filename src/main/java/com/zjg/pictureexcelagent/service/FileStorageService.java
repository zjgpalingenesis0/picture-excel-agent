package com.zjg.pictureexcelagent.service;

import com.zjg.pictureexcelagent.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AppConfig appConfig;

    public String storeUploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFileName = UUID.randomUUID().toString() + "." + fileExtension;

        Path uploadPath = Paths.get(appConfig.getStorage().getUploadDir());
        Path targetPath = uploadPath.resolve(newFileName);

        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Stored uploaded file: {} -> {}", originalFilename, newFileName);

        return targetPath.toString();
    }

    public String generateOutputFileName(String taskId, String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        String baseName = getFileNameWithoutExtension(originalFileName);
        String outputFileName = baseName + "_" + taskId + ".xlsx";

        Path outputPath = Paths.get(appConfig.getStorage().getOutputDir());
        Path targetPath = outputPath.resolve(outputFileName);

        return targetPath.toString();
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "tmp";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "tmp";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String getFileNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "output";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return filename;
        }
        return filename.substring(0, lastDotIndex);
    }

    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public void deleteFile(String filePath) throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
        log.info("Deleted file: {}", filePath);
    }
}
