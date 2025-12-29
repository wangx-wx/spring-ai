package com.example.wx.controller;

import com.example.wx.annotation.UserIp;
import com.example.wx.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/24 21:26
 */
@RestController
@Tag(name = "Chat APIs")
@RequestMapping("/api/v1")
public class ChatController {
    private final ChatService chatService;


    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送指定参数以获得模型响应。
     * 1. 当发送提示为空时，将返回一条错误消息。
     * 2. 当发送模型时，允许为空，当参数有值和时
     * 在模型配置列表中，调用对应的模型。如果没有返回错误。
     * 如果型号参数为空，则设置默认型号。qwen-plus
     * 3. 前端传递的chatId聊天内存是Object类型的，不能重复
     */
    @UserIp
    @PostMapping("/chat")
    @Operation(summary = "DashScope Flux Chat")
    public Flux<String> chat(
            HttpServletResponse response,
            @Validated @RequestBody String prompt,
            @RequestHeader(value = "model", required = false) String model,
            @RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-chat") String chatId
    ) {
        response.setCharacterEncoding("UTF-8");
        return chatService.chat(chatId, model, prompt);
    }

    /**
     * 深度思考聊天接口，提供增强的推理能力。
     * 1. 当发送提示为空时，将返回一条错误消息。
     * 2. 当发送模型时，允许为空，当参数有值且在模型配置列表中时，调用对应的模型。
     * 3. 前端传递的chatId聊天内存是Object类型的，不能重复。
     */
    @UserIp
    @PostMapping("/deep-thinking/chat")
    @Operation(summary = "Deep Thinking Chat with Enhanced Reasoning")
    public Flux<String> deepThinkingChat(
            HttpServletResponse response,
            @Validated @RequestBody String prompt,
            @RequestHeader(value = "model", required = false) String model,
            @RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-deepthink-chat") String chatId
    ) {
        response.setCharacterEncoding("UTF-8");
        return chatService.deepThinkingChat(chatId, model, prompt);
    }


}
