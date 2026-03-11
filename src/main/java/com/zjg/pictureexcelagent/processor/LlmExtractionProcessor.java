package com.zjg.pictureexcelagent.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjg.pictureexcelagent.enums.TaskStatus;
import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.llm.StructuredDataExtractor;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmExtractionProcessor implements ImageProcessor {

    private final StructuredDataExtractor dataExtractor;
    private final ObjectMapper objectMapper;

    @Override
    public void process(ProcessingTask task) throws ProcessingException {
        try {
            task.addLog("开始大模型结构化抽取");

            if (task.getOcrResult() == null) {
                throw new ProcessingException("OCR结果不存在，无法进行数据抽取");
            }

            String ocrText = task.getOcrResult().getRawText();
            String extractionRule = task.getExtractionRule();
            String fileName = task.getOriginalFileName();

            com.zjg.pictureexcelagent.model.ExtractedData extractedData = dataExtractor.extract(ocrText, extractionRule, fileName);

            // Convert to ProcessingTask.ExtractedData
            ProcessingTask.ExtractedData taskExtractedData = new ProcessingTask.ExtractedData();
            taskExtractedData.setDataType(extractedData.getDataType());
            taskExtractedData.getRecords().addAll(extractedData.getRecords());
            taskExtractedData.getMetadata().putAll(extractedData.getMetadata());
            taskExtractedData.setRawJson(extractedData.getRawJson());

            task.setExtractedData(taskExtractedData);
            task.setStatus(TaskStatus.EXTRACTION_COMPLETED);

            task.addLog(String.format("结构化抽取完成，提取到 %d 条记录", taskExtractedData.getRecords().size()));

            log.info("LLM extraction completed for task: {}, records: {}",
                    task.getTaskId(), taskExtractedData.getRecords().size());

        } catch (Exception e) {
            log.error("LLM extraction failed for task: {}", task.getTaskId(), e);
            throw new ProcessingException("大模型数据抽取失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
