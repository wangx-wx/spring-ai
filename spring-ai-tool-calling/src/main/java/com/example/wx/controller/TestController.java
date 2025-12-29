package com.example.wx.controller;

import com.example.wx.compontent.DateTimeTools;
import com.example.wx.compontent.MathTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 17:56
 */
@RestController
public class TestController {
    private ChatClient chatClient;

    @Autowired
    private ChatModel chatModel;

    public TestController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/test")
    public String test(@RequestParam(required = false, defaultValue = "现在几点了", value = "param") String param) {
        return chatClient.prompt("Can you set an alarm 10 minutes from now?")
                .tools(new DateTimeTools())
                .call()
                .content();
    }

    @GetMapping("/compute")
    public String compute() {
        ToolCallingManager toolCallingManager = DefaultToolCallingManager.builder().build();
        // 窗口记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        String conversationId = UUID.randomUUID().toString();

        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(ToolCallbacks.from(new MathTools()))
                .internalToolExecutionEnabled(false)
                .build();
        Prompt prompt = new Prompt(
                List.of(new SystemMessage("You are a helpful assistant."), new UserMessage("20/6")),
                chatOptions
        );

        chatMemory.add(conversationId, prompt.getInstructions());

        Prompt promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);
        ChatResponse chatResponse = chatModel.call(promptWithMemory);
        chatMemory.add(conversationId, chatResponse.getResult().getOutput());

        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(promptWithMemory, chatResponse);
            chatMemory.add(conversationId, toolExecutionResult.conversationHistory()
                    .get(toolExecutionResult.conversationHistory().size() - 1));
            promptWithMemory = new Prompt(chatMemory.get(conversationId), chatOptions);
            chatResponse = chatModel.call(promptWithMemory);
            chatMemory.add(conversationId, chatResponse.getResult().getOutput());
        }
        UserMessage userMessage = new UserMessage("what did i ask you earlier");
        chatMemory.add(conversationId, userMessage);
        return chatModel.call(new Prompt(chatMemory.get(conversationId))).getResult().getOutput().toString();

    }
}
