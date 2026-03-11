package com.zjg.pictureexcelagent.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.zjg.pictureexcelagent.model.ExtractedData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class EasyExcelWriter implements ExcelWriter {

    @Override
    public File write(ExtractedData data, String outputFileName) throws IOException {
        File outputFile = new File(outputFileName);
        outputFile.getParentFile().mkdirs();

        try {
            List<List<Object>> excelData = convertToExcelData(data);
            List<List<String>> head = buildHead(data);

            log.info("Writing Excel with {} columns and {} data rows", head.size(), excelData.size());

            WriteCellStyle headWriteCellStyle = new WriteCellStyle();
            headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            WriteFont headWriteFont = new WriteFont();
            headWriteFont.setFontHeightInPoints((short) 11);
            headWriteFont.setBold(true);
            headWriteCellStyle.setWriteFont(headWriteFont);
            headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

            WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
            contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);

            HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                    new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);

            EasyExcel.write(outputFile)
                    .head(head)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .sheet("Sheet1")
                    .doWrite(excelData);

            log.info("Successfully wrote Excel file: {}", outputFile.getAbsolutePath());
            return outputFile;

        } catch (Exception e) {
            log.error("Failed to write Excel file: {}", outputFileName, e);
            throw new IOException("Excel写入失败: " + e.getMessage(), e);
        }
    }

    private List<List<Object>> convertToExcelData(ExtractedData data) {
        List<List<Object>> excelData = new ArrayList<>();

        if (data.getRecords().isEmpty()) {
            return excelData;
        }

        // 获取所有字段（表头）
        Map<String, Object> firstRecord = data.getRecords().get(0);
        List<String> headers = new ArrayList<>(firstRecord.keySet());

        // 按表头顺序组织数据
        for (Map<String, Object> record : data.getRecords()) {
            List<Object> row = new ArrayList<>();
            for (String header : headers) {
                row.add(record.get(header));
            }
            excelData.add(row);
        }

        log.info("Converted {} records to Excel format", excelData.size());
        return excelData;
    }

    private List<List<String>> buildHead(ExtractedData data) {
        List<List<String>> head = new ArrayList<>();

        if (!data.getRecords().isEmpty()) {
            Map<String, Object> firstRecord = data.getRecords().get(0);
            for (String key : firstRecord.keySet()) {
                List<String> headColumn = new ArrayList<>();
                headColumn.add(key);
                head.add(headColumn);
            }
        }

        return head;
    }

    @Override
    public String getWriterName() {
        return "EasyExcel Writer";
    }
}
