package com.example.wx.service.impl;

import com.example.wx.entiy.tools.ToolCallResp;
import com.example.wx.service.ToolsService;
import com.example.wx.tools.ToolsInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:06
 */
@Slf4j
@Service
public class ToolsServiceImpl implements ToolsService {

    private final ChatClient chatClient;
    private final ToolCallingManager toolCallingManager;
    private final ToolsInit toolsInit;

    public ToolsServiceImpl(
            @Qualifier("dashscopeChatClient") ChatClient chatClient,
            ToolCallingManager toolCallingManager,
            ToolsInit toolsInit) {
        this.chatClient = chatClient;
        this.toolCallingManager = toolCallingManager;
        this.toolsInit = toolsInit;
    }

    @Override
    public ToolCallResp chat(String prompt) {
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(toolsInit.getTools())
                .internalToolExecutionEnabled(false)
                .build();

        Prompt userPrompt = new Prompt(prompt, chatOptions);

        ChatResponse response = chatClient.prompt(userPrompt)
                .call().chatResponse();

        log.info("ChatResponse: {}", response);
        assert response != null;
        List<AssistantMessage.ToolCall> toolCalls = response.getResult().getOutput().getToolCalls();
        log.info("ToolCalls: {}", toolCalls);
        String responseByLLm = response.getResult().getOutput().getText();
        log.info("Response by LLM: {}", responseByLLm);

        // execute tools with no chat memory messages.
        var tcr = ToolCallResp.TCR();

        if (!toolCalls.isEmpty()) {
            tcr = ToolCallResp.startExecute(responseByLLm, toolCalls.get(0).name(), toolCalls.get(0).arguments());
            log.info("Start ToolCallResp: {}", tcr);

            ToolExecutionResult toolExecutionResult = null;

            try {
                toolExecutionResult = toolCallingManager.executeToolCalls(new Prompt(prompt, chatOptions), response);
                log.info("ToolExecutionResult: {}", toolExecutionResult);
                tcr.setToolEndTime(LocalDateTime.now());
            } catch (Exception e) {
                tcr.setStatus(ToolCallResp.ToolState.FAILURE);
                tcr.setErrorMessage(e.getMessage());
                tcr.setToolEndTime(LocalDateTime.now());
                tcr.setToolCostTime((long) tcr.getToolEndTime().getNano() - tcr.getToolStartTime().getNano());
            }

            String llmCallResponse = "";
            if (Objects.nonNull(toolExecutionResult)) {
                ChatResponse finalResponse = chatClient.prompt().messages(toolExecutionResult.conversationHistory()).call().chatResponse();
                llmCallResponse = finalResponse.getResult().getOutput().getText();
            }
            tcr.setStatus(ToolCallResp.ToolState.SUCCESS);
            tcr.setToolResult(llmCallResponse);
            tcr.setToolCostTime((long)tcr.getToolEndTime().getNano() - tcr.getToolStartTime().getNano());
            log.info("End ToolCallResp: {}", tcr);
        } else {
            log.info("ToolCalls is empty, no tool execution needed.");
            tcr.setToolResponse(responseByLLm);
        }
        return tcr;
    }
}
