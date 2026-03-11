package com.zjg.pictureexcelagent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjg.pictureexcelagent.config.DashScopeConfig;
import com.zjg.pictureexcelagent.exception.LlmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashScopeClient {

    private final DashScopeConfig config;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public String generateText(String prompt) throws LlmException {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(config.getBaseUrl())
                    .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                    .build();

            Map<String, Object> requestBody = buildRequestBody(prompt);

            log.debug("Sending request to DashScope: {}", objectMapper.writeValueAsString(requestBody));

            String response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(config.getMaxRetries(), Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof java.io.IOException))
                    .block();

            log.info("Received response from DashScope: {}", response);

            return extractContentFromResponse(response);

        } catch (Exception e) {
            log.error("Failed to generate text with DashScope", e);
            throw new LlmException("DashScope调用失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());

        // Build messages array (OpenAI format)
        List<Map<String, String>> messages = new ArrayList<>();

        // System message
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个专业的数据提取助手，擅长从OCR识别的文本中提取结构化数据，并输出JSON格式。");
        messages.add(systemMsg);

        // User message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        return requestBody;
    }

    private String extractContentFromResponse(String response) throws LlmException {
        try {
            log.info("DashScope raw response: {}", response);

            JsonNode root = objectMapper.readTree(response);

            // Check for error
            if (root.has("error")) {
                JsonNode errorNode = root.get("error");
                String message = errorNode.has("message") ? errorNode.get("message").asText() : "Unknown error";
                String type = errorNode.has("type") ? errorNode.get("type").asText() : "unknown";
                throw new LlmException("DashScope API error [" + type + "]: " + message);
            }

            // OpenAI format: choices[0].message.content
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.path("message");
                JsonNode content = message.path("content");
                if (!content.isMissingNode() && !content.isNull()) {
                    return content.asText();
                }
            }

            throw new LlmException("Invalid response format: missing content field. Response: " + response);
        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("Failed to parse DashScope response: " + e.getMessage() + ". Response: " + response, e);
        }
    }

    public Mono<String> generateTextAsync(String prompt) {
        return Mono.fromCallable(() -> generateText(prompt));
    }
}
