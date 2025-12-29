package com.example.wx.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:09
 */
public interface VideoService {
    String analyzeVideo(String prompt, MultipartFile video) throws IOException;
}
