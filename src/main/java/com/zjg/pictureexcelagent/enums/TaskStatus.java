package com.zjg.pictureexcelagent.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    OCR_COMPLETED("OCR完成"),
    EXTRACTION_COMPLETED("数据抽取完成"),
    VALIDATION_COMPLETED("校验完成"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }
}
