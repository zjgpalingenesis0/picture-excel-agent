package com.zjg.pictureexcelagent.config;

import com.zjg.pictureexcelagent.enums.OcrEngineType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ocr")
public class OcrConfig {
    private OcrEngineType defaultEngine;
    private double confidenceThreshold;
    private String tessDataPath;
    private String language;
}
