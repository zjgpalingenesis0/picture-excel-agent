package com.zjg.pictureexcelagent.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ExtractedData {
    private String dataType;
    private List<Map<String, Object>> records;
    private Map<String, Object> metadata;
    private JsonNode rawJson;
    private List<ValidationError> validationErrors;

    public ExtractedData() {
        this.records = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.validationErrors = new ArrayList<>();
    }

    public void addRecord(Map<String, Object> record) {
        this.records.add(record);
    }

    @Data
    public static class ValidationError {
        private String field;
        private String message;
        private Object value;
    }
}
