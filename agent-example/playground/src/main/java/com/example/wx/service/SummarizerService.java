package com.example.wx.service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:03
 */
public interface SummarizerService {
    Flux<String> summary(MultipartFile file, String url);
}
