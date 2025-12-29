package com.example.wx.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 15:56
 */
@RestController
@RequestMapping("/model")
public class ChatModelController {
    private static final String DEFAULT_PROMPT = "你好，介绍下你自己吧。";

    private final ChatModel DeepSeekChatModel;

    public ChatModelController (ChatModel chatModel) {
        this.DeepSeekChatModel = chatModel;
    }

    /**
     * 最简单的使用方式，没有任何 LLMs 参数注入。
     *
     * @return String types.
     */
    @GetMapping("/simple/chat")
    public String simpleChat () {
        return DeepSeekChatModel.call(new Prompt(DEFAULT_PROMPT)).getResult().getOutput().getText();
    }
}
