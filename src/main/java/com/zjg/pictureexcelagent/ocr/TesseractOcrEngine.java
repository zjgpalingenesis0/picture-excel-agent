package com.zjg.pictureexcelagent.ocr;

import com.zjg.pictureexcelagent.config.OcrConfig;
import com.zjg.pictureexcelagent.exception.OcrException;
import com.zjg.pictureexcelagent.model.ProcessingTask.OcrResult;
import com.zjg.pictureexcelagent.model.ProcessingTask.TextBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ocr.default-engine", havingValue = "tesseract")
public class TesseractOcrEngine implements OcrEngine {

    private final OcrConfig ocrConfig;

    @Override
    public OcrResult recognize(File imageFile) throws OcrException {
        long startTime = System.currentTimeMillis();
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(ocrConfig.getTessDataPath());
            tesseract.setLanguage(ocrConfig.getLanguage());
            tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_DEFAULT);
            tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);

            log.info("Starting Tesseract OCR recognition for file: {}", imageFile.getName());

            String result = tesseract.doOCR(imageFile);

            // Load image as BufferedImage for getWords()
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            List<Word> words = tesseract.getWords(bufferedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD);

            OcrResult ocrResult = new OcrResult();
            ocrResult.setRawText(result);
            ocrResult.setTextBlocks(convertToTextBlocks(words));

            long processingTime = System.currentTimeMillis() - startTime;
            ocrResult.setProcessingTimeMs(processingTime);
            ocrResult.setConfidence(calculateAverageConfidence(words));

            log.info("Tesseract OCR recognition completed for file: {}, processing time: {}ms",
                    imageFile.getName(), processingTime);

            return ocrResult;

        } catch (Exception e) {
            log.error("Tesseract OCR recognition failed for file: {}", imageFile.getName(), e);
            throw new OcrException("Tesseract OCR识别失败: " + e.getMessage(), e);
        }
    }

    private List<TextBlock> convertToTextBlocks(List<Word> words) {
        List<TextBlock> textBlocks = new ArrayList<>();
        for (Word word : words) {
            TextBlock block = new TextBlock();
            block.setText(word.getText());
            block.setConfidence(word.getConfidence());
            textBlocks.add(block);
        }
        return textBlocks;
    }

    private double calculateAverageConfidence(List<Word> words) {
        if (words == null || words.isEmpty()) {
            return 0.0;
        }
        return words.stream()
                .mapToDouble(Word::getConfidence)
                .average()
                .orElse(0.0);
    }

    @Override
    public String getEngineName() {
        return "Tesseract OCR";
    }

    @Override
    public boolean isAvailable() {
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(ocrConfig.getTessDataPath());
            return true;
        } catch (Exception e) {
            log.warn("Tesseract OCR is not available: {}", e.getMessage());
            return false;
        }
    }
}
