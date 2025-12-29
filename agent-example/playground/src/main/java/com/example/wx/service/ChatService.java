package com.example.wx.service;

import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/24 21:28
 */
public interface ChatService {
    Flux<String> chat(String chatId, String model, String prompt);

    Flux<String> deepThinkingChat(String chatId, String model, String prompt);
}
