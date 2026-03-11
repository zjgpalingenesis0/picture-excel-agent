package com.zjg.pictureexcelagent.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonGenerationProcessor implements ImageProcessor {

    private final ObjectMapper objectMapper;

    @Override
    public void process(ProcessingTask task) throws ProcessingException {
        try {
            task.addLog("开始生成JSON格式数据");

            if (task.getExtractedData() == null) {
                throw new ProcessingException("提取的数据不存在，无法生成JSON");
            }

            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(task.getExtractedData().getRawJson());

            task.addLog("JSON数据生成完成");
            task.addLog("数据预览: " + (json.length() > 200 ? json.substring(0, 200) + "..." : json));

            log.info("JSON generation completed for task: {}", task.getTaskId());

        } catch (IOException e) {
            log.error("JSON generation failed for task: {}", task.getTaskId(), e);
            throw new ProcessingException("JSON生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
