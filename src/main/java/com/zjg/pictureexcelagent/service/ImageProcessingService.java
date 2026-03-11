package com.zjg.pictureexcelagent.service;

import com.zjg.pictureexcelagent.constants.ExtractionRules;
import com.zjg.pictureexcelagent.enums.TaskStatus;
import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import com.zjg.pictureexcelagent.processor.ImageProcessor;
import com.zjg.pictureexcelagent.processor.OcrProcessor;
import com.zjg.pictureexcelagent.processor.LlmExtractionProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        task.setBatch(false);

        taskStore.put(taskId, task);

        log.info("Created task: {} for file: {}", taskId, originalFileName);

        return task;
    }

    /**
     * 创建批量处理任务
     * @param filePaths 文件路径列表
     * @param originalFileNames 原始文件名列表
     * @param extractionRule 提取规则
     * @return 批量处理任务
     */
    public ProcessingTask createBatchTask(List<String> filePaths, List<String> originalFileNames, String extractionRule) {
        String taskId = UUID.randomUUID().toString();

        // 如果没有提供提取规则，使用默认的获奖证书提取规则
        if (extractionRule == null || extractionRule.isEmpty()) {
            extractionRule = ExtractionRules.DEFAULT_CERTIFICATE_RULE;
        }

        ProcessingTask task = new ProcessingTask();
        task.setTaskId(taskId);
        task.setFilePaths(filePaths);
        task.setOriginalFileNames(originalFileNames);
        task.setExtractionRule(extractionRule);
        task.setBatch(true);
        task.setCurrentFileIndex(0);

        // 对于批量任务，使用第一个文件名作为主文件名
        if (!originalFileNames.isEmpty()) {
            task.setOriginalFileName(originalFileNames.get(0));
        }

        taskStore.put(taskId, task);

        log.info("Created batch task: {} for {} files", taskId, filePaths.size());

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

        // 如果是批量任务，使用批量处理逻辑
        if (task.isBatch()) {
            return processBatchTask(task);
        }

        // 单文件任务处理逻辑
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

    /**
     * 处理批量任务
     * 遍历所有文件，执行OCR和数据提取，合并结果到一个Excel文件
     */
    private ProcessingTask processBatchTask(ProcessingTask task) throws ProcessingException {
        try {
            List<String> filePaths = task.getFilePaths();
            List<String> originalFileNames = task.getOriginalFileNames();

            if (filePaths == null || filePaths.isEmpty()) {
                throw new ProcessingException("批量任务没有文件");
            }

            task.addLog(String.format("开始批量处理 %d 个文件", filePaths.size()));
            task.setStatus(TaskStatus.PROCESSING);

            // 创建合并的 ExtractedData
            ProcessingTask.ExtractedData mergedExtractedData = new ProcessingTask.ExtractedData();
            mergedExtractedData.setDataType("certificate");
            int totalRecords = 0;
            int successCount = 0;
            int failCount = 0;

            // 获取OCR引擎和数据提取器
            OcrProcessor ocrProcessor = processors.stream()
                    .filter(p -> p instanceof OcrProcessor)
                    .map(p -> (OcrProcessor) p)
                    .findFirst()
                    .orElseThrow(() -> new ProcessingException("OCR处理器未找到"));

            LlmExtractionProcessor extractionProcessor = processors.stream()
                    .filter(p -> p instanceof LlmExtractionProcessor)
                    .map(p -> (LlmExtractionProcessor) p)
                    .findFirst()
                    .orElseThrow(() -> new ProcessingException("数据提取处理器未找到"));

            // 遍历所有文件
            for (int i = 0; i < filePaths.size(); i++) {
                String filePath = filePaths.get(i);
                String originalFileName = originalFileNames.get(i);
                task.setCurrentFileIndex(i);

                try {
                    task.addLog(String.format("处理文件 (%d/%d): %s", i + 1, filePaths.size(), originalFileName));

                    // 创建临时任务用于处理单个文件
                    ProcessingTask tempTask = new ProcessingTask();
                    tempTask.setTaskId(task.getTaskId() + "_temp_" + i);
                    tempTask.setFilePath(filePath);
                    tempTask.setOriginalFileName(originalFileName);
                    tempTask.setExtractionRule(task.getExtractionRule());
                    tempTask.setLogs(new ArrayList<>());

                    // 执行OCR
                    ocrProcessor.process(tempTask);

                    // 执行数据提取
                    extractionProcessor.process(tempTask);

                    // 合并结果
                    ProcessingTask.ExtractedData fileExtractedData = tempTask.getExtractedData();
                    if (fileExtractedData != null && fileExtractedData.getRecords() != null) {
                        mergedExtractedData.getRecords().addAll(fileExtractedData.getRecords());
                        totalRecords += fileExtractedData.getRecords().size();
                    }

                    successCount++;
                    task.addLog(String.format("文件 %s 处理成功，提取到 %d 条记录",
                            originalFileName,
                            fileExtractedData != null ? fileExtractedData.getRecords().size() : 0));

                } catch (Exception e) {
                    failCount++;
                    task.addLog(String.format("文件 %s 处理失败: %s", originalFileName, e.getMessage()));
                    log.error("Failed to process file in batch: {}", originalFileName, e);
                    // 继续处理其他文件，不中断整个批次
                }
            }

            task.setExtractedData(mergedExtractedData);

            // 生成Excel文件
            String outputFilePath = fileStorageService.generateOutputFileName(
                    task.getTaskId(), "batch_result");
            excelGenerationService.generateExcel(mergedExtractedData, outputFilePath);

            task.setOutputFilePath(outputFilePath);
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            task.addLog(String.format("批量处理完成: 成功 %d, 失败 %d, 总记录数 %d",
                    successCount, failCount, totalRecords));

            log.info("Batch task processing completed: {}, success: {}, failed: {}, total records: {}",
                    task.getTaskId(), successCount, failCount, totalRecords);

            return task;

        } catch (Exception e) {
            log.error("Batch task processing failed: {}", task.getTaskId(), e);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            task.addLog("批量任务处理失败: " + e.getMessage());
            throw new ProcessingException("批量任务处理失败: " + e.getMessage(), e);
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
