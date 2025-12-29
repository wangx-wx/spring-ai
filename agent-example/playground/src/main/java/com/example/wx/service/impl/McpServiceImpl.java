package com.example.wx.service.impl;

import com.example.wx.entiy.mcp.McpServer;
import com.example.wx.entiy.mcp.McpServerConfig;
import com.example.wx.entiy.mcp.McpServerContainer;
import com.example.wx.entiy.tools.ToolCallResp;
import com.example.wx.service.McpService;
import com.example.wx.utils.McpServerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpStdioClientProperties;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author wangx
 * @description
 * @create 2025/8/25 21:23
 */
@Service
public class McpServiceImpl implements McpService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ToolCallbackProvider tools;
    private final ToolCallingManager toolCallingManager;
    private final McpStdioClientProperties mcpStdioClientProperties;

    private static final Logger logger = LoggerFactory.getLogger(McpServiceImpl.class);

    public McpServiceImpl(
            @Qualifier("deepseekChatClient") ChatClient chatClient,
            ToolCallingManager toolCallingManager,
            ToolCallbackProvider tools,
            McpStdioClientProperties mcpStdioClientProperties,
            ObjectMapper objectMapper
    ) {
        this.chatClient = chatClient;
        this.toolCallingManager = toolCallingManager;
        this.mcpStdioClientProperties = mcpStdioClientProperties;
        this.objectMapper = objectMapper;
        this.tools = tools;
    }

    @Override
    public ToolCallResp chat(String prompt) {
        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools.getToolCallbacks())
                .internalToolExecutionEnabled(false)
                .build();

        ChatResponse response = chatClient.prompt(new Prompt(prompt, chatOptions))
                .call().chatResponse();

        logger.debug("chat response: {}", response);
        assert response != null;
        List<AssistantMessage.ToolCall> toolCalls =
                response.getResult().getOutput().getToolCalls();
        logger.debug("toolCalls: {}", toolCalls);
        String respLLM = response.getResult().getOutput().getText();
        logger.debug("respLLM: {}", respLLM);

        ToolCallResp tcr = ToolCallResp.TCR();
        if (!toolCalls.isEmpty()) {
            tcr = ToolCallResp.startExecute(
                    respLLM,
                    toolCalls.get(0).name(),
                    toolCalls.get(0).arguments()
            );
            logger.debug("Start ToolCallResp: {}", tcr);
            ToolExecutionResult toolExecutionResult = null;

            try {
                toolExecutionResult = toolCallingManager.executeToolCalls(new Prompt(prompt, chatOptions), response);
                tcr.setToolEndTime(LocalDateTime.now());
            } catch (Exception e) {
                tcr.setStatus(ToolCallResp.ToolState.FAILURE);
                tcr.setErrorMessage(e.getMessage());
                tcr.setToolEndTime(LocalDateTime.now());
                tcr.setToolCostTime((long) tcr.getToolEndTime().getNano() - tcr.getToolStartTime().getNano());
                logger.error("ToolCallResp error: {}", tcr);
            }

            String llmCallResponse = "";
            if (Objects.nonNull(toolExecutionResult)) {
                ChatResponse finalChatResponse = chatClient.prompt().messages(toolExecutionResult.conversationHistory()).call().chatResponse();
                if (finalChatResponse != null) {
                    llmCallResponse = finalChatResponse.getResult().getOutput().getText();
                }
                StringBuffer sb = new StringBuffer();
                toolExecutionResult.conversationHistory().stream()
                        .filter(message -> message instanceof ToolResponseMessage)
                        .forEach(message ->{
                            ToolResponseMessage toolResponseMessage = (ToolResponseMessage) message;
                            toolResponseMessage.getResponses().forEach(toolResponse -> {
                                sb.append(toolResponse.responseData());
                            });
                        });
                tcr.setToolResponse(sb.toString());
            }
            tcr.setStatus(ToolCallResp.ToolState.SUCCESS);
            tcr.setToolResult(llmCallResponse);
            tcr.setToolCostTime((long) tcr.getToolEndTime().getNano() - tcr.getToolStartTime().getNano());
            logger.debug("End ToolCallResp: {}", tcr);
        } else {
            logger.debug("toolCalls is empty");
            tcr.setToolResult(respLLM);
        }

        return tcr;
    }

    @Override
    public ToolCallResp run(String id, Map<String, String> env, String prompt) throws IOException {
        Optional<McpServer> runMcpServer = McpServerContainer.getServerById(id);
        if (runMcpServer.isEmpty()){
            logger.error("McpServer not found, id: {}", id);
            return ToolCallResp.TCR();
        }

        String runMcpServerName = runMcpServer.get().getName();
        McpServerConfig mcpServerConfig = McpServerUtils.getMcpServerConfig();
        McpStdioClientProperties.Parameters parameters = new McpStdioClientProperties.Parameters(
                mcpServerConfig.getMcpServers().get(runMcpServerName).command(),
                mcpServerConfig.getMcpServers().get(runMcpServerName).args(),
                env
        );
        if (parameters.command().startsWith("java")){
            String oldMcpLibsPath = McpServerUtils.getLibsPath(parameters.args());
            String rewriteMcpLibsAbsPath = McpServerUtils.getMcpLibsAbsPath(McpServerUtils.getLibsPath(parameters.args()));

            parameters.args().remove(oldMcpLibsPath);
            parameters.args().remove(rewriteMcpLibsAbsPath);
        }
        String mcpServerConfigJson = objectMapper.writeValueAsString(mcpServerConfig);
        mcpStdioClientProperties.setServersConfiguration(new ByteArrayResource(mcpServerConfigJson.getBytes()));
        return chat(prompt);
    }
}
