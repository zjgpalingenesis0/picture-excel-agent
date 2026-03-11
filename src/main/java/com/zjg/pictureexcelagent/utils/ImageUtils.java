package com.zjg.pictureexcelagent.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
@UtilityClass
public class ImageUtils {

    /**
     * 百度OCR图片限制常量
     */
    private static final int BAIDU_OCR_MAX_WIDTH = 4096;
    private static final int BAIDU_OCR_MAX_HEIGHT = 4096;
    private static final long BAIDU_OCR_MAX_BASE64_SIZE = 4 * 1024 * 1024; // 4MB

    public boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".bmp")
                || name.endsWith(".gif") || name.endsWith(".tiff");
    }

    public BufferedImage readImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    public String getImageFormat(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "JPEG";
        } else if (name.endsWith(".png")) {
            return "PNG";
        } else if (name.endsWith(".bmp")) {
            return "BMP";
        } else if (name.endsWith(".gif")) {
            return "GIF";
        } else if (name.endsWith(".tiff")) {
            return "TIFF";
        }
        return "UNKNOWN";
    }

    /**
     * 检查图片是否符合百度OCR尺寸要求
     * @param image 图片对象
     * @return 是否符合要求
     */
    public boolean checkBaiduOcrSizeLimit(BufferedImage image) {
        if (image == null) {
            return false;
        }
        return image.getWidth() <= BAIDU_OCR_MAX_WIDTH
                && image.getHeight() <= BAIDU_OCR_MAX_HEIGHT;
    }

    /**
     * 缩放图片以符合百度OCR尺寸要求
     * @param image 原始图片
     * @return 缩放后的图片（如果不需要缩放则返回原图片）
     */
    public BufferedImage scaleImageForBaiduOcr(BufferedImage image) {
        if (image == null) {
            return null;
        }

        // 如果图片尺寸符合要求，直接返回
        if (checkBaiduOcrSizeLimit(image)) {
            return image;
        }

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // 计算缩放比例，保持宽高比
        double scale = Math.min(
            (double) BAIDU_OCR_MAX_WIDTH / originalWidth,
            (double) BAIDU_OCR_MAX_HEIGHT / originalHeight
        );

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // 创建缩放后的图片
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = scaledImage.createGraphics();
        try {
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        } finally {
            g2d.dispose();
        }

        log.info("图片已缩放: {}x{} -> {}x{} (缩放比例: {})",
            originalWidth, originalHeight, newWidth, newHeight, String.format("%.2f", scale));

        return scaledImage;
    }
}
