package com.example.wx.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/13 14:25
 */
public interface RagService {
    void importDocuments();

    Flux<ChatResponse> retrieve(String message);

    String delete(String[] idList);
}
