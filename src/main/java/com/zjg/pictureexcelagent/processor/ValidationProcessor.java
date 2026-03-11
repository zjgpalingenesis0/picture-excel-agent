package com.zjg.pictureexcelagent.processor;

import com.zjg.pictureexcelagent.enums.TaskStatus;
import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import com.zjg.pictureexcelagent.validator.DataValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ValidationProcessor implements ImageProcessor {

    @Override
    public void process(ProcessingTask task) throws ProcessingException {
        try {
            task.addLog("开始数据校验");

            if (task.getExtractedData() == null) {
                throw new ProcessingException("提取的数据不存在，无法进行校验");
            }

            validateData(task);

            task.setStatus(TaskStatus.VALIDATION_COMPLETED);

            task.addLog("数据校验完成");

            log.info("Data validation completed for task: {}", task.getTaskId());

        } catch (Exception e) {
            log.error("Data validation failed for task: {}", task.getTaskId(), e);
            throw new ProcessingException("数据校验失败: " + e.getMessage(), e);
        }
    }

    private void validateData(ProcessingTask task) throws ProcessingException {
        ProcessingTask.ExtractedData data = task.getExtractedData();

        var errors = DataValidator.validate(data);

        if (!errors.isEmpty()) {
            data.getValidationErrors().addAll(errors);
            task.addLog("数据校验发现 " + errors.size() + " 个问题");
        }

        if (data.getRecords().isEmpty()) {
            task.addLog("警告: 未提取到任何数据记录");
        } else {
            task.addLog(String.format("验证了 %d 条数据记录", data.getRecords().size()));
        }

        if (data.getDataType() != null && !data.getDataType().isEmpty()) {
            task.addLog(String.format("数据类型: %s", data.getDataType()));
        }
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
