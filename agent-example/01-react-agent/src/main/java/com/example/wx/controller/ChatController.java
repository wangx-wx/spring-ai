package com.example.wx.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.example.wx.interceptor.ToolErrorInterceptor;
import com.example.wx.tools.SearchTool;
import com.example.wx.tools.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/26 16:49
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ReactAgent agent;

    public ChatController(ChatModel chatModel) {
        ToolCallback weatherTool = FunctionToolCallback.builder("weather", new WeatherTool())
                .inputType(String.class)
                .description("天气查询工具").build();
        ToolCallback searchTool = FunctionToolCallback.builder("search", new SearchTool())
                .description("搜索工具")
                .inputType(String.class)
                .build();
        // 创建 Agent
        this.agent = ReactAgent.builder()
                .name("service_agent")
                .model(chatModel)
                .instruction("You are a helpful weather forecast assistant.")
                .returnReasoningContents(true)
                .tools(searchTool, weatherTool)
                .interceptors(new ToolErrorInterceptor())
                .chatOptions(DashScopeChatOptions.builder().enableThinking(true).build())
                .enableLogging(true)
                .build();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<NodeOutput> stream(@RequestParam(value = "query", defaultValue = "查询杭州天气并推荐活动") String query)
            throws GraphRunnerException {
        return this.agent.stream(query)
                .doOnError(error -> System.err.println("流错误: " + error.getMessage()))
                .doOnComplete(()-> System.out.println("流完成"))
                .map(output -> {
                    System.out.println("节点输出: " + output.getClass().getName());
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        OutputType type = streamingOutput.getOutputType();
                        // 处理模型推理的流式输出
                        Message message = streamingOutput.message();

                        if (message != null) {

                            // 🔑 关键：检查是否是 DeepSeek 消息，获取思考内容
                            if (message instanceof DeepSeekAssistantMessage deepSeekMsg) {
                                String reasoningContent = deepSeekMsg.getReasoningContent();
                                if (reasoningContent != null && !reasoningContent.isEmpty()) {
                                    System.out.println("思考内容: " + reasoningContent);
                                }
                            }
                        }
                        if (type == OutputType.AGENT_MODEL_STREAMING) {
                            // 流式增量内容，逐步显示
                            System.out.print(streamingOutput.message().getText());
                        } else if (type == OutputType.AGENT_MODEL_FINISHED) {
                            // 模型推理完成，可获取完整响应
                            System.out.println("\n模型输出完成");
                        }

                        // 处理工具调用完成（目前不支持 STREAMING）
                        if (type == OutputType.AGENT_TOOL_FINISHED) {
                            System.out.println("工具调用完成: " + output.node());
                        }

                        // 对于 Hook 节点，通常只关注完成事件（如果Hook没有有效输出可以忽略）
                        if (type == OutputType.AGENT_HOOK_FINISHED) {
                            System.out.println("Hook 执行完成: " + output.node());
                        }
                        return streamingOutput;
                    }
                    return output;
                });
    }

}
