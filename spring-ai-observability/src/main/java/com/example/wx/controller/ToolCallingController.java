package com.example.wx.controller;

import com.example.wx.tools.TestTools;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/6/18 22:00
 */
@RestController
@RequestMapping("/observability/tools")
public class ToolCallingController {

    public final ChatClient chatClient;

    public ToolCallingController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public Flux<String> chat(@RequestParam(name = "prompt", defaultValue = "现在的时间") String prompt, HttpServletResponse response) {

        response.setContentType("text/event-stream;charset=UTF-8");
        return chatClient.prompt(prompt).tools(new TestTools()).stream().content();
    }

}
