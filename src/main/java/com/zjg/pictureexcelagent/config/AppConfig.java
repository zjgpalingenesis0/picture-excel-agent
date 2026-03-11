package com.zjg.pictureexcelagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private Storage storage;
    private Processing processing;

    @Data
    public static class Storage {
        private String uploadDir;
        private String outputDir;
        private String tempDir;

        public File getUploadDirFile() {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }

        public File getOutputDirFile() {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }

        public File getTempDirFile() {
            File dir = new File(tempDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
    }

    @Data
    public static class Processing {
        private int timeoutSeconds;
        private int maxRetryCount;
    }
}
