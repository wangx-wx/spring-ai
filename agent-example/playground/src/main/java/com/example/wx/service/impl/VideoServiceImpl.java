package com.example.wx.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.wx.service.VideoService;
import com.example.wx.utils.FilesUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:09
 */
@Service
public class VideoServiceImpl implements VideoService {
    private static final String DEFAULT_MODEL = "qwen-vl-max-latest";
    private static final String TEMP_IMAGE_DIR = "temp-frames";

    private final ChatClient daschScopeChatClient;

    public VideoServiceImpl(
            @Qualifier("dashscopeChatModel") ChatModel chatModel
    ) {

        this.daschScopeChatClient = ChatClient
                .builder(chatModel)
                .build();
    }

    /**
     * 分析视频内容并回答用户问题
     *
     * @param prompt    用户问题
     * @param videoFile 上传的视频文件
     * @return AI分析结果
     */
    @Override
    public String analyzeVideo(String prompt, MultipartFile videoFile) throws IOException {
        // 1. 验证视频格式
        if (!isSupportedFormat(videoFile)) {
            throw new IllegalArgumentException("不支持的视频格式");
        }

        // 2. 保存视频到临时文件
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), TEMP_IMAGE_DIR);
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // 3. 从视频中提取10帧
        List<File> frames = extractFrames(tempDir.toFile(), 10);


        // 4. 准备AI分析所需的媒体列表
        List<Media> mediaList = new ArrayList<>();
        for (File frame : frames) {
            mediaList.add(new Media(
                    MimeTypeUtils.IMAGE_PNG,
                    new FileSystemResource(frame)
            ));
        }

        // 5. 创建包含问题和帧图片的用户消息
        UserMessage message =
                UserMessage.builder()
                        .text(prompt)
                        .media(mediaList)
                        .metadata(new HashMap<>()).build();

        // 6. 调用AI服务进行分析
        List<ChatResponse> response = daschScopeChatClient.prompt(
                        new Prompt(
                                message,
                                DashScopeChatOptions.builder()
                                        .withModel(DEFAULT_MODEL)
                                        .withMultiModel(true)
                                        .build()
                        ))
                .stream()
                .chatResponse()
                .collectList()
                .block();
        // 7. 处理并返回响应
        StringBuilder result = new StringBuilder();
        if (response != null) {
            for (ChatResponse chatResponse : response) {
                String outputContent = chatResponse.getResult().getOutput().getText();
                result.append(outputContent).append("\n");
            }
        }

        // 8. 清理临时文件
        cleanUpFiles(frames);

        return result.toString();
    }

    /**
     * 从视频中提取指定数量的帧图像
     *
     * @param videoFile  视频文件
     * @param frameCount 要提取的帧数
     * @return 图片文件列表，每帧一张图
     * @throws IOException 文件 IO 异常
     */
    private List<File> extractFrames(File videoFile, int frameCount) throws IOException {
        List<File> frames = new ArrayList<>();


        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            grabber.start();

            // 获取总帧数
            int totalFrames = grabber.getLengthInFrames();
            if (totalFrames <= 0) {
                throw new IOException("无法获取视频帧数，视频可能损坏");
            }

            // 若请求帧数 > 总帧数，自动调整为总帧数
            frameCount = Math.min(frameCount, totalFrames);
            int step = Math.max(1, totalFrames / frameCount); // 防止 step 为 0

            // 创建临时目录存储图片
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), TEMP_IMAGE_DIR);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            for (int i = 0; i < frameCount; i++) {
                int frameNumber = i * step;
                grabber.setFrameNumber(frameNumber);
                Frame frame = grabber.grabImage();

                if (frame == null) {
                    System.err.println("第 " + frameNumber + " 帧为空，跳过");
                    continue;
                }

                BufferedImage image = converter.convert(frame);

                // 保存图片为 PNG 文件
                File outputFile = tempDir.resolve(UUID.randomUUID().toString() + ".png").toFile();
                ImageIO.write(image, "png", outputFile);
                frames.add(outputFile);
            }
        } catch (Exception e) {
            throw new IOException("视频帧提取失败：" + e.getMessage(), e);
        }
        return frames;
    }

    /**
     * 清理临时文件
     * @param files 要删除的文件列表
     */
    private void cleanUpFiles(List<File> files) {
        for (File file : files) {
            file.delete();
        }
    }


    /**
     * 检查视频格式是否支持
     *
     * @param file 上传的文件
     * @return 是否支持
     */
    private boolean isSupportedFormat(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.startsWith("video/mp4") ||
                        contentType.startsWith("video/quicktime"));
    }
}
