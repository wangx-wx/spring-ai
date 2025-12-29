package com.example.wx.controller;

import com.example.wx.annotation.UserIp;
import com.example.wx.entiy.result.Result;
import com.example.wx.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 21:41
 */
@RestController
@Tag(name = "Image APIs")
@RequestMapping("/api/v1")
public class ImageController {
    private static final String DEFAULT_IMAGE_STYLE = "摄影写实";

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * 图像识别
     * prompt can be empty
     */
    @UserIp
    @PostMapping("/image2text")
    @Operation(summary = "DashScope Image Recognition")
    public Flux<String> image2text(
            @Validated @RequestParam(value = "prompt", required = false, defaultValue = "请总结图片内容") String prompt,
            @Validated @RequestParam("image") MultipartFile image
    ) {
        if (image.isEmpty()) {
            return Flux.just("No image file provided");
        }
        Flux<String> res;
        try {
            res = imageService.image2Text(prompt, image);
        } catch (Exception e) {
            return Flux.just(e.getMessage());
        }
        return res;
    }

    @UserIp
    @GetMapping("/text2image")
    @Operation(summary = "DashScope Image Generation")
    public Result<Void> text2Image(
            HttpServletResponse response,
            @Validated @RequestParam("prompt") String prompt,
            @RequestParam(value = "style", required = false, defaultValue = DEFAULT_IMAGE_STYLE) String style,
            @RequestParam(value = "resolution", required = false, defaultValue = "1080*1080") String resolution
    ) {

        imageService.text2Image(prompt, resolution, style, response);
        return Result.success();
    }
}
