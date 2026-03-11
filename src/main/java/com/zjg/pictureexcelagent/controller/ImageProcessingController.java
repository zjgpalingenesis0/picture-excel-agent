package com.zjg.pictureexcelagent.controller;

import com.zjg.pictureexcelagent.dto.response.ProcessResultResponse;
import com.zjg.pictureexcelagent.enums.TaskStatus;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import com.zjg.pictureexcelagent.service.FileStorageService;
import com.zjg.pictureexcelagent.service.ImageProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/image/excel")
@RequiredArgsConstructor
@Tag(name = "图片转Excel", description = "图片识别并转换为Excel文件接口")
public class ImageProcessingController {

    private final ImageProcessingService imageProcessingService;
    private final FileStorageService fileStorageService;

    @PostMapping("/process/image")
    @Operation(summary = "处理单张图片", description = "上传图片文件，进行OCR识别并转换为Excel文件")
    public ResponseEntity<ProcessResultResponse> processImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "extractionRule", required = false)
            @Parameter(description = "数据提取规则（可选）") String extractionRule,
            @RequestParam(value = "async", defaultValue = "false")
            @Parameter(description = "是否异步处理") boolean async) {

        try {
            log.info("Received image processing request for file: {}", file.getOriginalFilename());

            String filePath = fileStorageService.storeUploadFile(file);

            ProcessingTask task = imageProcessingService.createTask(
                    filePath, file.getOriginalFilename(), extractionRule);

            if (async) {
                imageProcessingService.processAsync(task.getTaskId());
                return ResponseEntity.ok(ProcessResultResponse.processing(task.getTaskId()));
            } else {
                imageProcessingService.processTask(task.getTaskId());

                if (task.getStatus() == TaskStatus.COMPLETED && task.getOutputFilePath() != null) {
                    String downloadUrl = "/api/v1/task/" + task.getTaskId() + "/download";
                    return ResponseEntity.ok(ProcessResultResponse.success(
                            task.getTaskId(), downloadUrl));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ProcessResultResponse.error(
                                    task.getTaskId(),
                                    task.getErrorMessage() != null ? task.getErrorMessage() : "处理失败"));
                }
            }

        } catch (Exception e) {
            log.error("Failed to process image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProcessResultResponse.error(null, "处理失败: " + e.getMessage()));
        }
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "查询任务状态", description = "根据任务ID查询处理状态")
    public ResponseEntity<ProcessResultResponse> getTaskStatus(
            @PathVariable String taskId) {
        ProcessingTask task = imageProcessingService.getTask(taskId);

        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        ProcessResultResponse response = ProcessResultResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .message(getStatusMessage(task.getStatus()))
                .downloadUrl(task.getOutputFilePath() != null ?
                        "/api/v1/task/" + task.getTaskId() + "/download" : null)
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .logs(task.getLogs())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/task/{taskId}/download")
    @Operation(summary = "下载结果文件", description = "下载处理完成的Excel文件")
    public ResponseEntity<Resource> downloadResult(@PathVariable String taskId) {
        ProcessingTask task = imageProcessingService.getTask(taskId);

        if (task == null || task.getOutputFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            File file = new File(task.getOutputFilePath());
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String originalFileName = task.getOriginalFileName();
            String downloadFileName = originalFileName != null ?
                    originalFileName.replaceAll("\\.[^.]+$", "") + ".xlsx" :
                    "result.xlsx";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + downloadFileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download file for task: {}", taskId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/task/{taskId}/logs")
    @Operation(summary = "查询任务日志", description = "获取任务的处理日志")
    public ResponseEntity<ProcessResultResponse> getTaskLogs(@PathVariable String taskId) {
        ProcessingTask task = imageProcessingService.getTask(taskId);

        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        ProcessResultResponse response = ProcessResultResponse.builder()
                .taskId(task.getTaskId())
                .status(task.getStatus())
                .logs(task.getLogs())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    @Operation(summary = "获取所有任务", description = "获取所有处理任务列表")
    public ResponseEntity<List<ProcessingTask>> getAllTasks() {
        return ResponseEntity.ok(imageProcessingService.getAllTasks());
    }

    @PostMapping(value = "/process/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "批量处理图片", description = "上传多张图片文件，合并处理生成一个Excel文件。所有图片的数据将合并到一个Excel中，每条记录会标注来源文件。")
    public ResponseEntity<ProcessResultResponse> processBatch(
            @RequestPart(value = "files", required = true)
            @Parameter(
                description = "图片文件列表（支持多个文件）。knife4j可能无法直接选择多个文件，建议使用Postman或cURL测试。",
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @Schema(description = "图片文件列表", type = "array", format = "binary")
            MultipartFile[] files,
            @RequestParam(value = "extractionRule", required = false)
            @Parameter(description = "数据提取规则（可选）")
            @Schema(description = "数据提取规则")
            String extractionRule,
            @RequestParam(value = "async", defaultValue = "true")
            @Parameter(description = "是否异步处理（默认true，批量处理建议使用异步）")
            @Schema(description = "是否异步处理")
            boolean async) {

        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest()
                        .body(ProcessResultResponse.error(null, "没有上传文件"));
            }

            log.info("Received batch processing request for {} files", files.length);

            // 保存所有文件
            java.util.List<String> filePaths = new java.util.ArrayList<>();
            java.util.List<String> originalFileNames = new java.util.ArrayList<>();

            for (MultipartFile file : files) {
                String filePath = fileStorageService.storeUploadFile(file);
                filePaths.add(filePath);
                originalFileNames.add(file.getOriginalFilename());
            }

            // 创建批量任务
            ProcessingTask task = imageProcessingService.createBatchTask(
                    filePaths, originalFileNames, extractionRule);

            if (async) {
                // 异步处理
                imageProcessingService.processAsync(task.getTaskId());
                return ResponseEntity.ok(ProcessResultResponse.processing(task.getTaskId()));
            } else {
                // 同步处理
                imageProcessingService.processTask(task.getTaskId());

                if (task.getStatus() == TaskStatus.COMPLETED && task.getOutputFilePath() != null) {
                    String downloadUrl = "/api/v1/task/" + task.getTaskId() + "/download";
                    return ResponseEntity.ok(ProcessResultResponse.success(
                            task.getTaskId(), downloadUrl));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ProcessResultResponse.error(
                                    task.getTaskId(),
                                    task.getErrorMessage() != null ? task.getErrorMessage() : "处理失败"));
                }
            }

        } catch (Exception e) {
            log.error("Failed to process batch images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProcessResultResponse.error(null, "批量处理失败: " + e.getMessage()));
        }
    }

    private String getStatusMessage(TaskStatus status) {
        return switch (status) {
            case PENDING -> "任务待处理";
            case PROCESSING -> "任务处理中";
            case OCR_COMPLETED -> "OCR识别完成";
            case EXTRACTION_COMPLETED -> "数据抽取完成";
            case VALIDATION_COMPLETED -> "数据校验完成";
            case COMPLETED -> "任务完成";
            case FAILED -> "任务失败";
            case CANCELLED -> "任务已取消";
        };
    }
}
