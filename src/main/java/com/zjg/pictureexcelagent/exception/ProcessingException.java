package com.zjg.pictureexcelagent.exception;

import lombok.Getter;

@Getter
public class ProcessingException extends RuntimeException {
    private final String errorCode;

    public ProcessingException(String message) {
        super(message);
        this.errorCode = "PROCESSING_ERROR";
    }

    public ProcessingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ProcessingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PROCESSING_ERROR";
    }
}
