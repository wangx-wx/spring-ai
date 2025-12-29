package com.example.wx.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 21:43
 */
public interface ImageService {
    Flux<String> image2Text(String prompt, MultipartFile image) throws IOException;

    void text2Image(String prompt, String resolution, String style, HttpServletResponse response);
}
