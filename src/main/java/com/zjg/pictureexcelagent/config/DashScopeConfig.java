package com.zjg.pictureexcelagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeConfig {
    private String apiKey;
    private String model;
    private int timeout;
    private int maxRetries;
    private String baseUrl;
}
