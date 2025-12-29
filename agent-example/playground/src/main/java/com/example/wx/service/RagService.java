package com.example.wx.service;

import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 21:59
 */
public interface RagService {
    Flux<String> ragChat(String chatId, String prompt);
}
