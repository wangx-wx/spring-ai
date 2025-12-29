package com.example.wx.controller;

import com.example.wx.annotation.UserIp;
import com.example.wx.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 21:58
 */
@RestController
@Tag(name = "RAG APIs")
@RequestMapping("/api/v1")
public class RAGController {

    private final RagService ragService;

    public RAGController(RagService ragService) {
        this.ragService = ragService;
    }

    @UserIp
    @GetMapping("/rag")
    @Operation(summary = "DashScope RAG")
    public Flux<String> ragChat(
            HttpServletResponse response,
            @Validated @RequestParam("prompt") String prompt,
            @RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-rag") String chatId
    ) {
        response.setCharacterEncoding("UTF-8");
        return ragService.ragChat(chatId, prompt);
    }
}
