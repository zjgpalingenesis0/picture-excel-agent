package com.zjg.pictureexcelagent.utils;

import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@UtilityClass
public class ImageUtils {

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
}
