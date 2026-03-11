package com.zjg.pictureexcelagent.processor;

import com.zjg.pictureexcelagent.enums.TaskStatus;
import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import com.zjg.pictureexcelagent.ocr.OcrEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrProcessor implements ImageProcessor {

    private final List<OcrEngine> ocrEngines;

    @Override
    public void process(ProcessingTask task) throws ProcessingException {
        try {
            task.addLog("开始OCR识别");

            File imageFile = new File(task.getFilePath());

            // 查找可用的OCR引擎
            OcrEngine availableEngine = findAvailableEngine();
            if (availableEngine == null) {
                throw new ProcessingException("没有可用的OCR引擎，请检查配置");
            }

            task.addLog("使用OCR引擎: " + availableEngine.getEngineName());

            ProcessingTask.OcrResult ocrResult = availableEngine.recognize(imageFile);

            task.setOcrResult(ocrResult);
            task.setStatus(TaskStatus.OCR_COMPLETED);

            task.addLog(String.format("OCR识别完成，置信度: %.2f%%", ocrResult.getConfidence()));
            task.addLog(String.format("识别到 %d 个文本块", ocrResult.getTextBlocks().size()));

            log.info("OCR processing completed for task: {}, engine: {}, confidence: {}",
                    task.getTaskId(), availableEngine.getEngineName(), ocrResult.getConfidence());

        } catch (Exception e) {
            log.error("OCR processing failed for task: {}", task.getTaskId(), e);
            throw new ProcessingException("OCR识别失败: " + e.getMessage(), e);
        }
    }

    private OcrEngine findAvailableEngine() {
        for (OcrEngine engine : ocrEngines) {
            if (engine.isAvailable()) {
                log.debug("Found available OCR engine: {}", engine.getEngineName());
                return engine;
            }
        }
        log.error("No available OCR engine found");
        return null;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
