package com.zjg.pictureexcelagent.processor;

import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OcrResultNormalizer implements ImageProcessor {

    @Override
    public void process(ProcessingTask task) throws ProcessingException {
        try {
            task.addLog("开始OCR结果标准化");

            if (task.getOcrResult() == null) {
                throw new ProcessingException("OCR结果不存在，无法进行标准化");
            }

            String normalizedText = normalizeText(task.getOcrResult().getRawText());
            task.getOcrResult().setRawText(normalizedText);

            task.addLog("OCR结果标准化完成");

            log.info("OCR result normalization completed for task: {}", task.getTaskId());

        } catch (Exception e) {
            log.error("OCR result normalization failed for task: {}", task.getTaskId(), e);
            throw new ProcessingException("OCR结果标准化失败: " + e.getMessage(), e);
        }
    }

    private String normalizeText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        String normalized = rawText
                .replaceAll("\\s+", " ")
                .replaceAll("([0-9])\\s+([0-9])", "$1$2")
                .replaceAll("([a-zA-Z])\\s+([a-zA-Z])", "$1$2")
                .trim();

        return normalized;
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
