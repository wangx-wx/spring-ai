package com.example.wx.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/6/17 22:32
 */
@RestController
@RequestMapping("/observability/chat")
public class ChatClientController {

    private final ChatClient chatClient;

    public ChatClientController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @GetMapping("/stream")
    public Flux<String> streamChat(@RequestParam(value = "prompt", defaultValue = "hi") String prompt) {
        return chatClient.prompt(prompt).stream().content();
    }
    @GetMapping
    public String chat(@RequestParam(value = "prompt", defaultValue = "hi") String prompt) {
        return chatClient.prompt(prompt).call().content();
    }
}
