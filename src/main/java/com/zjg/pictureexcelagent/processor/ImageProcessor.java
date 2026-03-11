package com.zjg.pictureexcelagent.processor;

import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;

public interface ImageProcessor {
    void process(ProcessingTask task) throws ProcessingException;

    int getOrder();
}
