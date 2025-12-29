package com.example.wx.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 15:57
 */
@RestController
@RequestMapping("client")
public class ChatClientController {


    private final ChatClient DeepSeekChatClient;

    public ChatClientController (DeepSeekChatModel chatModel) {

        this.DeepSeekChatClient = ChatClient.builder(chatModel).defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DeepSeekChatOptions.builder().temperature(0.7d).build()).build();
    }
    @GetMapping(value = "/ai/customOptions")
    public ChatResponse customOptions () {

        return this.DeepSeekChatClient.prompt(new Prompt(
                        "Generate the names of 5 famous pirates.",
                        DeepSeekChatOptions.builder().temperature(0.75).build())
                ).call()
                .chatResponse();
    }
}
