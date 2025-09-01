package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.example.wx.conf.TodoChatFlowFactory;
import com.example.wx.conf.TodoSubGraphFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/1 21:35
 */
@RestController
@RequestMapping("/assistant")
public class ChatFlowController {
    private final CompiledGraph mainGraph;
    private final ChatClient chatClient;

    public ChatFlowController(ChatClient.Builder chatClientBuilder) throws Exception {
        chatClient = chatClientBuilder.build();
        // 构建子图
        CompiledGraph subGraph = TodoSubGraphFactory.build(chatClient);
        // 构建主图
        this.mainGraph = TodoChatFlowFactory.build(chatClient, subGraph);
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("userInput") String userInput
    ) throws Exception {
        Map<String, Object> input = new HashMap<>();
        input.put("session_id", sessionId);
        input.put("user_input", userInput);

        var stateOpt = mainGraph.invoke(input, RunnableConfig.builder().threadId(sessionId).build());
        OverAllState state = stateOpt.orElseThrow();

        Map<String, Object> result = new HashMap<>();
        result.put("reply", state.value("answer").orElse(""));
        result.put("tasks", state.value("tasks").orElse(List.of()));
        return result;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam(value = "userInput", defaultValue = "你好") String userInput
    ) throws Exception {
        return chatClient.prompt(userInput).call().content();
    }
}
