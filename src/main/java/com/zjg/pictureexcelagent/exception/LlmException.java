package com.zjg.pictureexcelagent.exception;

public class LlmException extends ProcessingException {
    public LlmException(String message) {
        super("LLM_ERROR", message);
    }

    public LlmException(String message, Throwable cause) {
        super("LLM_ERROR", message, cause);
    }
}
