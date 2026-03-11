package com.zjg.pictureexcelagent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjg.pictureexcelagent.exception.LlmException;
import com.zjg.pictureexcelagent.model.ExtractedData;
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
public class StructuredDataExtractor {

    private final DashScopeClient dashScopeClient;
    private final ObjectMapper objectMapper;

    public ExtractedData extract(String ocrText, String extractionRule, String fileName) throws LlmException {
        try {
            String prompt = PromptTemplate.buildExtractionPrompt(ocrText, extractionRule, fileName);
            String response = dashScopeClient.generateText(prompt);

            log.debug("LLM extraction response: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);
            ExtractedData extractedData = new ExtractedData();

            JsonNode dataTypeNode = jsonNode.path("dataType");
            if (!dataTypeNode.isMissingNode()) {
                extractedData.setDataType(dataTypeNode.asText());
            }

            JsonNode recordsNode = jsonNode.path("records");
            if (recordsNode.isArray()) {
                for (JsonNode recordNode : recordsNode) {
                    Map<String, Object> record = objectMapper.convertValue(recordNode, Map.class);
                    extractedData.addRecord(record);
                }
            }

            JsonNode metadataNode = jsonNode.path("metadata");
            if (!metadataNode.isMissingNode()) {
                Map<String, Object> metadata = objectMapper.convertValue(metadataNode, Map.class);
                extractedData.setMetadata(metadata);
            }

            extractedData.setRawJson(jsonNode);

            log.info("Successfully extracted {} records from OCR text", extractedData.getRecords().size());
            return extractedData;

        } catch (Exception e) {
            log.error("Failed to extract structured data", e);
            throw new LlmException("数据抽取失败: " + e.getMessage(), e);
        }
    }

    public List<ExtractedData.ValidationError> validate(String jsonData, String validationRules) throws LlmException {
        try {
            String prompt = PromptTemplate.buildValidationPrompt(jsonData, validationRules);
            String response = dashScopeClient.generateText(prompt);

            log.debug("LLM validation response: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);
            List<ExtractedData.ValidationError> errors = new ArrayList<>();

            JsonNode errorsNode = jsonNode.path("errors");
            if (errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    ExtractedData.ValidationError error = new ExtractedData.ValidationError();
                    error.setField(errorNode.path("field").asText());
                    error.setMessage(errorNode.path("message").asText());
                    error.setValue(errorNode.path("value").asText());
                    errors.add(error);
                }
            }

            return errors;

        } catch (Exception e) {
            log.error("Failed to validate data", e);
            throw new LlmException("数据验证失败: " + e.getMessage(), e);
        }
    }

    public ExtractedData correct(String ocrText, ExtractedData extractedData) throws LlmException {
        try {
            String extractedJson = objectMapper.writeValueAsString(extractedData.getRawJson());
            String prompt = PromptTemplate.buildCorrectionPrompt(ocrText, extractedJson);
            String response = dashScopeClient.generateText(prompt);

            log.debug("LLM correction response: {}", response);

            JsonNode jsonNode = objectMapper.readTree(response);

            ExtractedData correctedData = new ExtractedData();
            correctedData.setDataType(extractedData.getDataType());

            JsonNode recordsNode = jsonNode.path("records");
            if (recordsNode.isArray()) {
                for (JsonNode recordNode : recordsNode) {
                    Map<String, Object> record = objectMapper.convertValue(recordNode, Map.class);
                    correctedData.addRecord(record);
                }
            }

            JsonNode metadataNode = jsonNode.path("metadata");
            if (!metadataNode.isMissingNode()) {
                Map<String, Object> metadata = objectMapper.convertValue(metadataNode, Map.class);
                correctedData.setMetadata(metadata);
            }

            correctedData.setRawJson(jsonNode);

            log.info("Successfully corrected extracted data");
            return correctedData;

        } catch (Exception e) {
            log.error("Failed to correct data", e);
            throw new LlmException("数据纠错失败: " + e.getMessage(), e);
        }
    }
}
