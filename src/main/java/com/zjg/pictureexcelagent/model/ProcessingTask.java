package com.zjg.pictureexcelagent.model;

import com.zjg.pictureexcelagent.enums.TaskStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class ProcessingTask {
    private String taskId;
    private String originalFileName;
    private String filePath;
    private TaskStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String outputFilePath;
    private List<String> logs;
    private OcrResult ocrResult;
    private ExtractedData extractedData;
    private String extractionRule;

    public ProcessingTask() {
        this.logs = new ArrayList<>();
        this.status = TaskStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addLog(String log) {
        this.logs.add(log);
        this.updatedAt = LocalDateTime.now();
    }

    @Data
    public static class OcrResult {
        private String rawText;
        private List<TextBlock> textBlocks;
        private double confidence;
        private long processingTimeMs;
    }

    @Data
    public static class TextBlock {
        private String text;
        private double confidence;
        private BoundingBox boundingBox;
    }

    @Data
    public static class BoundingBox {
        private int x;
        private int y;
        private int width;
        private int height;
    }

    @Data
    public static class ExtractedData {
        private String dataType;
        private List<java.util.Map<String, Object>> records;
        private java.util.Map<String, Object> metadata;
        private com.fasterxml.jackson.databind.JsonNode rawJson;
        private java.util.List<ValidationError> validationErrors;

        public ExtractedData() {
            this.records = new java.util.ArrayList<>();
            this.metadata = new java.util.HashMap<>();
            this.validationErrors = new java.util.ArrayList<>();
        }

        @Data
        public static class ValidationError {
            private String field;
            private String message;
            private Object value;
        }
    }
}
