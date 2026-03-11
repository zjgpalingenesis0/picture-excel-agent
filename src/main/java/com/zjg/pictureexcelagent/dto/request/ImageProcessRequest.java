package com.zjg.pictureexcelagent.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageProcessRequest {
    @NotNull(message = "文件不能为空")
    private String fileName;

    private String extractionRule;

    private String outputFormat;

    private boolean async = false;
}
