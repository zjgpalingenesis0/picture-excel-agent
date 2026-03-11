package com.zjg.pictureexcelagent.ocr;

import com.baidu.aip.ocr.AipOcr;
import com.zjg.pictureexcelagent.config.BaiduOcrConfig;
import com.zjg.pictureexcelagent.exception.OcrException;
import com.zjg.pictureexcelagent.model.ProcessingTask.OcrResult;
import com.zjg.pictureexcelagent.model.ProcessingTask.TextBlock;
import com.zjg.pictureexcelagent.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ocr.default-engine", havingValue = "baidu")
public class BaiduOcrEngine implements OcrEngine {

    private static final int BAIDU_OCR_MAX_BASE64_SIZE = 4 * 1024 * 1024; // 4MB

    private final BaiduOcrConfig baiduOcrConfig;
    private AipOcr client;

    private synchronized AipOcr getClient() {
        if (client == null) {
            client = new AipOcr(
                baiduOcrConfig.getAppId(),
                baiduOcrConfig.getApiKey(),
                baiduOcrConfig.getSecretKey()
            );
            // 设置网络连接参数
            client.setConnectionTimeoutInMillis(5000);
            client.setSocketTimeoutInMillis(60000);
            log.info("Baidu OCR client initialized with app-id: {}", baiduOcrConfig.getAppId());
        }
        return client;
    }

    @Override
    public OcrResult recognize(File imageFile) throws OcrException {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Starting Baidu OCR recognition for file: {}", imageFile.getName());

            // 读取图片
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) {
                throw new OcrException("无法读取图片文件");
            }

            int originalWidth = bufferedImage.getWidth();
            int originalHeight = bufferedImage.getHeight();
            log.debug("原始图片尺寸: {}x{}", originalWidth, originalHeight);

            // 检查并缩放图片以符合百度OCR限制
            BufferedImage processedImage = ImageUtils.scaleImageForBaiduOcr(bufferedImage);

            if (processedImage != bufferedImage) {
                log.info("图片已自动缩放以符合百度OCR API限制");
            }

            // 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            // 检查Base64编码后的大小
            long base64Size = (long) Math.ceil(imageBytes.length * 4.0 / 3.0);
            if (base64Size > BAIDU_OCR_MAX_BASE64_SIZE) {
                log.warn("图片Base64编码后大小({} bytes)超过百度OCR限制({} bytes)，可能仍会失败",
                    base64Size, BAIDU_OCR_MAX_BASE64_SIZE);
            }

            // 设置识别参数
            HashMap<String, String> options = new HashMap<>();
            options.put("detect_direction", "true");  // 检测图像朝向
            options.put("language_type", "CHN_ENG");  // 中英文混合
            options.put("detect_language", "true");   // 检测语言

            // 根据配置选择普通版或高精度版
            org.json.JSONObject res;
            if (baiduOcrConfig.isUseAccurate()) {
                log.debug("Using Baidu OCR Accurate (High Precision) mode");
                res = getClient().accurateGeneral(imageBytes, options);
            } else {
                log.debug("Using Baidu OCR General mode");
                res = getClient().general(imageBytes, options);
            }

            log.debug("Baidu OCR response: {}", res.toString());

            OcrResult ocrResult = parseBaiduResponse(res);

            long processingTime = System.currentTimeMillis() - startTime;
            ocrResult.setProcessingTimeMs(processingTime);

            log.info("Baidu OCR recognition completed for file: {}, processing time: {}ms, words count: {}",
                    imageFile.getName(), processingTime, ocrResult.getTextBlocks().size());

            return ocrResult;

        } catch (Exception e) {
            log.error("Baidu OCR recognition failed for file: {}", imageFile.getName(), e);
            throw new OcrException("百度OCR识别失败: " + e.getMessage(), e);
        }
    }

    private OcrResult parseBaiduResponse(org.json.JSONObject response) throws OcrException {
        OcrResult ocrResult = new OcrResult();
        List<TextBlock> textBlocks = new ArrayList<>();

        try {
            // 检查错误码
            if (response.has("error_code")) {
                int errorCode = response.getInt("error_code");
                String errorMsg = response.optString("error_msg", "Unknown error");
                throw new OcrException("百度OCR API错误 [" + errorCode + "]: " + errorMsg);
            }

            // 获取识别结果
            if (response.has("words_result")) {
                org.json.JSONArray wordsArray = response.getJSONArray("words_result");

                StringBuilder fullText = new StringBuilder();

                for (int i = 0; i < wordsArray.length(); i++) {
                    org.json.JSONObject wordObj = wordsArray.getJSONObject(i);
                    String text = wordObj.optString("words", "");

                    TextBlock textBlock = new TextBlock();
                    textBlock.setText(text);
                    textBlock.setConfidence(95.0); // 百度OCR默认置信度

                    textBlocks.add(textBlock);
                    fullText.append(text).append("\n");
                }

                ocrResult.setRawText(fullText.toString());
                ocrResult.setTextBlocks(textBlocks);
                ocrResult.setConfidence(95.0);

                log.debug("Parsed {} text blocks from Baidu OCR response", textBlocks.size());
            }

        } catch (Exception e) {
            throw new OcrException("解析百度OCR响应失败: " + e.getMessage(), e);
        }

        return ocrResult;
    }

    @Override
    public String getEngineName() {
        return "百度OCR";
    }

    @Override
    public boolean isAvailable() {
        boolean available = baiduOcrConfig.getAppId() != null
                && !baiduOcrConfig.getAppId().isEmpty()
                && baiduOcrConfig.getApiKey() != null
                && !baiduOcrConfig.getApiKey().isEmpty()
                && baiduOcrConfig.getSecretKey() != null
                && !baiduOcrConfig.getSecretKey().isEmpty();

        if (!available) {
            log.warn("Baidu OCR is not available: missing configuration");
        }
        return available;
    }
}
