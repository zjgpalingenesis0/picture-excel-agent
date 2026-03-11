package com.zjg.pictureexcelagent.validator;

import com.zjg.pictureexcelagent.model.ProcessingTask;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UtilityClass
public class DataValidator {

    public List<ProcessingTask.ExtractedData.ValidationError> validate(ProcessingTask.ExtractedData data) {
        List<ProcessingTask.ExtractedData.ValidationError> errors = new ArrayList<>();

        if (data == null) {
            ProcessingTask.ExtractedData.ValidationError error = new ProcessingTask.ExtractedData.ValidationError();
            error.setField("data");
            error.setMessage("数据为空");
            errors.add(error);
            return errors;
        }

        if (data.getRecords() == null || data.getRecords().isEmpty()) {
            ProcessingTask.ExtractedData.ValidationError error = new ProcessingTask.ExtractedData.ValidationError();
            error.setField("records");
            error.setMessage("未提取到任何数据记录");
            errors.add(error);
        } else {
            validateRecords(data, errors);
        }

        return errors;
    }

    private void validateRecords(ProcessingTask.ExtractedData data, List<ProcessingTask.ExtractedData.ValidationError> errors) {
        for (int i = 0; i < data.getRecords().size(); i++) {
            Map<String, Object> record = data.getRecords().get(i);

            if (record == null || record.isEmpty()) {
                ProcessingTask.ExtractedData.ValidationError error = new ProcessingTask.ExtractedData.ValidationError();
                error.setField("record[" + i + "]");
                error.setMessage("记录为空");
                errors.add(error);
            }
        }
    }

    public boolean isValid(ProcessingTask.ExtractedData data) {
        return validate(data).isEmpty();
    }
}
