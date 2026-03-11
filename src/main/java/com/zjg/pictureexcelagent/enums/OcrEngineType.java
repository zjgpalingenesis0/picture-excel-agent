package com.zjg.pictureexcelagent.enums;

import lombok.Getter;

@Getter
public enum OcrEngineType {
    TESSERACT("Tesseract"),
    PADDLE("PaddleOCR"),
    BAIDU("百度OCR");

    private final String description;

    OcrEngineType(String description) {
        this.description = description;
    }
}
