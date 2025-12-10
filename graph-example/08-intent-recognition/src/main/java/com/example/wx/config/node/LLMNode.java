package com.example.wx.config.node;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.example.wx.domain.LLMConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;

/**
 * 通用 LLM 调用节点
 * <p>
 * 支持以下功能：
 * <ul>
 *   <li>System/User 消息模板化（使用 StringTemplate 语法）</li>
 *   <li>纯文本输出或结构化输出（自动解析为 Java 对象）</li>
 *   <li>动态输出类型（运行时从 state 获取类名）</li>
 * </ul>
 *
 * @author wangxiang
 * @create 2025/12/9 22:06
 */
@AllArgsConstructor
public class LLMNode implements NodeAction {

    private final ChatModel chatModel;
    private final LLMConfig llmConfig;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultOptions(buildOptions())
                .build();

        // 渲染 system 和 user 消息模板
        String systemMessage = renderSystemPrompt(state);
        String userMessage = renderUserPrompt(state);

        // 调用 LLM
        var callResponse = chatClient.prompt()
                .system(systemMessage)
                .user(userMessage)
                .call();

        // 处理输出
        return buildResult(callResponse, state);
    }

    /**
     * 渲染 System Prompt 模板
     * <p>
     * 使用 {@link PromptTemplate} 将 sysParams 中定义的变量从 state 获取并替换到模板中
     *
     * @param state 全局状态
     * @return 渲染后的 system prompt 字符串
     */
    private String renderSystemPrompt(OverAllState state) {
        Map<String, Object> params = resolveParams(llmConfig.getSysParams(), state);
        PromptTemplate template = new PromptTemplate(llmConfig.getSystemPrompt());
        return template.render(params);
    }

    /**
     * 渲染 User Prompt 模板
     * <p>
     * 两种模式：
     * <ol>
     *   <li>模板模式：userPrompt 不为空时，使用 userParams 进行变量替换</li>
     *   <li>简单模式：userPrompt 为空时，直接从 state 获取 queryKey 对应的值</li>
     * </ol>
     *
     * @param state 全局状态
     * @return 渲染后的 user prompt 字符串
     */
    private String renderUserPrompt(OverAllState state) {
        if (llmConfig.getUserPrompt() != null) {
            // 模板模式
            Map<String, Object> params = resolveParams(llmConfig.getUserParams(), state);
            PromptTemplate template = new PromptTemplate(llmConfig.getUserPrompt());
            return template.render(params);
        }
        // 简单模式：直接从 state 获取
        return state.value(llmConfig.getQueryKey(), "");
    }

    /**
     * 解析模板参数
     * <p>
     * 将配置中的参数 key 从 state 中获取实际值，创建线程安全的局部副本
     *
     * @param configParams 配置的参数 map（key 为变量名）
     * @param state        全局状态
     * @return 填充了实际值的参数 map
     */
    private Map<String, Object> resolveParams(Map<String, Object> configParams, OverAllState state) {
        if (configParams == null || configParams.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> resolved = new HashMap<>(configParams.size());
        for (String key : configParams.keySet()) {
            resolved.put(key, state.value(key).orElse(""));
        }
        return resolved;
    }

    /**
     * 构建输出结果
     * <p>
     * 根据 structuredOutput 配置决定输出模式：
     * <ul>
     *   <li>结构化输出：使用 entity() 自动解析为指定 Java 类型</li>
     *   <li>纯文本输出：返回原始字符串</li>
     * </ul>
     *
     * @param callResponse ChatClient 调用响应
     * @param state        全局状态（用于动态解析输出类型）
     * @return 包含输出结果的 map
     */
    private Map<String, Object> buildResult(ChatClient.CallResponseSpec callResponse, OverAllState state) {
        Map<String, Object> result = new HashMap<>();

        if (llmConfig.isStructuredOutput()) {
            // 结构化输出
            Class<?> outputType = resolveOutputType(state);
            if (outputType == null) {
                throw new IllegalStateException(
                        "structuredOutput=true but outputType is not specified. " +
                                "Please set outputType or outputTypeKey in LLMConfig.");
            }
            Object entity = callResponse.entity(outputType);
            result.put(llmConfig.getOutputKey(), entity);
        } else {
            // 纯文本输出
            String answer = Optional.ofNullable(callResponse.chatResponse())
                    .map(ChatResponse::getResult)
                    .map(Generation::getOutput)
                    .map(AbstractMessage::getText)
                    .orElse(null);
            result.put(llmConfig.getOutputKey(), answer);
        }
        return result;
    }

    /**
     * 解析输出类型
     * <p>
     * 优先级：outputTypeKey（动态从 state 获取类名）> outputType（静态配置）
     *
     * @param state 全局状态
     * @return 输出类型的 Class 对象，可能为 null
     */
    private Class<?> resolveOutputType(OverAllState state) {
        // 优先从 state 动态获取类名
        if (llmConfig.getOutputTypeKey() != null) {
            String className = state.value(llmConfig.getOutputTypeKey(), "");
            if (!className.isEmpty()) {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Failed to load output type class: " + className, e);
                }
            }
        }
        // 使用静态配置的输出类型
        return llmConfig.getOutputType();
    }

    private DashScopeChatOptions buildOptions() {
        return DashScopeChatOptions.builder()
                .model(llmConfig.getModel())
                .temperature(llmConfig.getTemperature())
                .topP(llmConfig.getTopP())
                .build();
    }

}