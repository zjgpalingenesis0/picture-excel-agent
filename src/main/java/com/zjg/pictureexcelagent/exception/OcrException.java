package com.zjg.pictureexcelagent.exception;

public class OcrException extends ProcessingException {
    public OcrException(String message) {
        super("OCR_ERROR", message);
    }

    public OcrException(String message, Throwable cause) {
        super("OCR_ERROR", message, cause);
    }
}
