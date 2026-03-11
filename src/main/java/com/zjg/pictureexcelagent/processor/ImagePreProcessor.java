package com.zjg.pictureexcelagent.processor;

import com.zjg.pictureexcelagent.enums.TaskStatus;
import com.zjg.pictureexcelagent.exception.ProcessingException;
import com.zjg.pictureexcelagent.model.ProcessingTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class ImagePreProcessor implements ImageProcessor {

    @Override
    public void process(ProcessingTask task) throws ProcessingException {
        try {
            task.addLog("开始图像预处理");
            task.setStatus(TaskStatus.PROCESSING);

            File imageFile = new File(task.getFilePath());
            if (!imageFile.exists()) {
                throw new ProcessingException("图片文件不存在: " + task.getFilePath());
            }

            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                throw new ProcessingException("无法读取图片文件，可能格式不支持");
            }

            task.addLog(String.format("图像信息: 宽度=%d, 高度=%d", image.getWidth(), image.getHeight()));
            task.addLog("图像预处理完成");

            log.info("Image preprocessing completed for task: {}", task.getTaskId());

        } catch (IOException e) {
            log.error("Image preprocessing failed for task: {}", task.getTaskId(), e);
            throw new ProcessingException("图像预处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
