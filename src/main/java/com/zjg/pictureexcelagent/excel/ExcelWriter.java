package com.zjg.pictureexcelagent.excel;

import com.zjg.pictureexcelagent.model.ExtractedData;

import java.io.File;
import java.io.IOException;

public interface ExcelWriter {
    File write(ExtractedData data, String outputFileName) throws IOException;

    String getWriterName();
}
