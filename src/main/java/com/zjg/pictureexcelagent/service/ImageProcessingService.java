package com.zjg.pictureexcelagent.service;

import com.zjg.pictureexcelagent.constants.ExtractionRules;
import com.zjg.pictureexcelagent.enums.TaskStatus;
import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import com.zjg.pictureexcelagent.processor.ImageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final List<ImageProcessor> processors;
    private final FileStorageService fileStorageService;
    private final ExcelGenerationService excelGenerationService;

    private final ConcurrentHashMap<String, ProcessingTask> taskStore = new ConcurrentHashMap<>();

    public ProcessingTask createTask(String filePath, String originalFileName, String extractionRule) {
        String taskId = UUID.randomUUID().toString();

        // 如果没有提供提取规则，使用默认的获奖证书提取规则
        if (extractionRule == null || extractionRule.isEmpty()) {
            extractionRule = ExtractionRules.DEFAULT_CERTIFICATE_RULE;
        }

        ProcessingTask task = new ProcessingTask();
        task.setTaskId(taskId);
        task.setFilePath(filePath);
        task.setOriginalFileName(originalFileName);
        task.setExtractionRule(extractionRule);

        taskStore.put(taskId, task);

        log.info("Created task: {} for file: {}", taskId, originalFileName);

        return task;
    }

    @Async
    public void processAsync(String taskId) {
        try {
            processTask(taskId);
        } catch (Exception e) {
            log.error("Async processing failed for task: {}", taskId, e);
            ProcessingTask task = taskStore.get(taskId);
            if (task != null) {
                task.setStatus(TaskStatus.FAILED);
                task.setErrorMessage(e.getMessage());
                task.setCompletedAt(LocalDateTime.now());
            }
        }
    }

    public ProcessingTask processTask(String taskId) throws ProcessingException {
        ProcessingTask task = taskStore.get(taskId);
        if (task == null) {
            throw new ProcessingException("TASK_NOT_FOUND", "任务不存在: " + taskId);
        }

        try {
            processors.sort(Comparator.comparingInt(ImageProcessor::getOrder));

            for (ImageProcessor processor : processors) {
                log.debug("Executing processor: {} for task: {}",
                        processor.getClass().getSimpleName(), taskId);
                processor.process(task);
            }

            String outputFilePath = fileStorageService.generateOutputFileName(
                    taskId, task.getOriginalFileName());

            ProcessingTask.ExtractedData extractedData = task.getExtractedData();
            if (extractedData != null) {
                excelGenerationService.generateExcel(extractedData, outputFilePath);
            }

            task.setOutputFilePath(outputFilePath);
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            task.addLog("任务处理完成");

            log.info("Task processing completed: {}", taskId);

            return task;

        } catch (Exception e) {
            log.error("Task processing failed: {}", taskId, e);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            task.addLog("任务处理失败: " + e.getMessage());
            throw new ProcessingException("任务处理失败: " + e.getMessage(), e);
        }
    }

    public ProcessingTask getTask(String taskId) {
        return taskStore.get(taskId);
    }

    public List<ProcessingTask> getAllTasks() {
        return List.copyOf(taskStore.values());
    }

    public void cleanupOldTasks(int maxAgeHours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(maxAgeHours);
        taskStore.entrySet().removeIf(entry -> {
            ProcessingTask task = entry.getValue();
            LocalDateTime completedAt = task.getCompletedAt();
            return completedAt != null && completedAt.isBefore(cutoff);
        });
        log.info("Cleaned up old tasks older than {} hours", maxAgeHours);
    }
}
