package com.example.wx.controller;

import com.example.wx.annotation.UserIp;
import com.example.wx.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:08
 */
@RestController
@Tag(name = "视频问答 APIs")
@RequestMapping("/api/v1")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }


    /**
     * 视频问答接口
     * @param prompt 用户问题（可选）
     * @param video 上传的视频文件（必传）
     * @return 视频内容分析结果（流式返回）
     */
    @UserIp
    @PostMapping("/video-qa")
    @Operation(summary = "基于视频内容的问答接口")
    public Flux<String> videoQuestionAnswering(
            @Validated @RequestParam(value = "prompt", required = false, defaultValue = "请总结这个视频的主要内容") String prompt,
            @NotNull @RequestParam("video") MultipartFile video
    ) {

        // 验证视频文件
        if (video.isEmpty()) {
            return Flux.just("错误：请上传有效的视频文件");
        }

        try {
            // 调用视频分析服务
            String analyzeVideo = videoService.analyzeVideo(prompt, video);
            return Flux.just(analyzeVideo);
        } catch (Exception e) {
            return Flux.just("视频处理失败：" + e.getMessage());
        }
    }
}
