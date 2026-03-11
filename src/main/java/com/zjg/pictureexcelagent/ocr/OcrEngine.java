package com.zjg.pictureexcelagent.ocr;

import com.zjg.pictureexcelagent.exception.OcrException;
import com.zjg.pictureexcelagent.model.ProcessingTask.OcrResult;

import java.io.File;

public interface OcrEngine {
    OcrResult recognize(File imageFile) throws OcrException;

    String getEngineName();

    boolean isAvailable();
}
