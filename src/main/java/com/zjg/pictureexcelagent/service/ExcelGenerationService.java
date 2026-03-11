package com.zjg.pictureexcelagent.service;

import com.zjg.pictureexcelagent.excel.ExcelWriter;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelGenerationService {

    private final ExcelWriter excelWriter;

    public File generateExcel(ProcessingTask.ExtractedData data, String outputFilePath) throws IOException {
        log.info("Generating Excel file: {}", outputFilePath);

        // Convert ProcessingTask.ExtractedData to a format ExcelWriter can use
        com.zjg.pictureexcelagent.model.ExtractedData excelData = new com.zjg.pictureexcelagent.model.ExtractedData();
        excelData.setDataType(data.getDataType());
        excelData.getRecords().addAll(data.getRecords());
        excelData.getMetadata().putAll(data.getMetadata());
        excelData.setRawJson(data.getRawJson());

        File excelFile = excelWriter.write(excelData, outputFilePath);

        log.info("Excel file generated successfully: {}", excelFile.getAbsolutePath());

        return excelFile;
    }
}
