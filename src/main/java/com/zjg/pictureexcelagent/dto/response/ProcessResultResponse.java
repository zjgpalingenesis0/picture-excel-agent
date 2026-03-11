package com.zjg.pictureexcelagent.dto.response;

import com.zjg.pictureexcelagent.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResultResponse {
    private String taskId;
    private TaskStatus status;
    private String message;
    private String downloadUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<String> logs;

    public static ProcessResultResponse success(String taskId, String downloadUrl) {
        return ProcessResultResponse.builder()
                .taskId(taskId)
                .status(TaskStatus.COMPLETED)
                .message("处理完成")
                .downloadUrl(downloadUrl)
                .completedAt(LocalDateTime.now())
                .build();
    }

    public static ProcessResultResponse processing(String taskId) {
        return ProcessResultResponse.builder()
                .taskId(taskId)
                .status(TaskStatus.PROCESSING)
                .message("任务处理中")
                .build();
    }

    public static ProcessResultResponse error(String taskId, String errorMessage) {
        return ProcessResultResponse.builder()
                .taskId(taskId)
                .status(TaskStatus.FAILED)
                .message(errorMessage)
                .build();
    }
}
