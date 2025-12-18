package com.example.wx.config.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private ChatClient chatClient;
    private ChatOptions chatOptions;
    //
    private String inputKey;
    private String outputKey;
    private String systemPrompt;
    private Map<String, Object> sysParams;
    private String userPrompt;
    private Map<String, Object> userParams;
    private String outputSchema;
    private String outputPackage;

    public LLMNode(Builder builder) {
        this.chatClient = builder.chatClient;
        this.chatOptions = builder.chatOptions;
        this.inputKey = builder.inputKey;
        this.outputKey = builder.outputKey;
        this.systemPrompt = builder.systemPrompt;
        this.sysParams = builder.sysParams;
        this.userPrompt = builder.userPrompt;
        this.userParams = builder.userParams;
        this.outputSchema = builder.outputSchema;
        this.outputPackage = builder.outputPackage;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {


        List<Message> messageList = new ArrayList<>();
        // 渲染 system 和 user 消息模板
        renderSystemPrompt(state, messageList);
        renderUserPrompt(state, messageList);

        Optional<String> value = state.value(this.inputKey, String.class);
        value.ifPresent(s -> messageList.add(new UserMessage(s)));

        // 调用 LLM
        var callResponse = this.chatClient.prompt()
                .messages(messageList)
                .options(this.chatOptions)
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
     */
    private void renderSystemPrompt(OverAllState state, List<Message> messageList) {
        if (!StringUtils.hasText(systemPrompt)) {
            return;
        }
        SystemPromptTemplate template = new SystemPromptTemplate(systemPrompt);
        Map<String, Object> params = resolveParams(this.sysParams, state);
        Message message = null;
        if (!params.isEmpty()) {
            message = template.createMessage(params);
        } else {
            message = template.createMessage();
        }
        messageList.add(message);
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
     */
    private void renderUserPrompt(OverAllState state, List<Message> messageList) {
        if (!StringUtils.hasText(userPrompt)) {
            return;
        }
        PromptTemplate template = new PromptTemplate(userPrompt);
        Map<String, Object> params = resolveParams(this.userParams, state);
        Message message = null;
        if (!params.isEmpty()) {
            message = template.createMessage(params);
        } else {
            message = template.createMessage();
        }
        messageList.add(message);
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
                    return null;
                }
            }
        }
        // 使用静态配置的输出类型
        return llmConfig.getOutputType();
    }

    public static class Builder {
        //
        private ChatClient chatClient;
        //
        private ChatOptions chatOptions;
        // 输入参数
        private String inputKey;
        private String outputKey;
        private String systemPrompt;
        private Map<String, Object> sysParams;
        private String userPrompt;
        private Map<String, Object> userParams;
        private String outputSchema;
        private String outputPackage;

        public Builder chatClient(ChatClient chatClient) {
            this.chatClient = chatClient;
            return this;
        }

        public Builder chatOptions(ChatOptions chatOptions) {
            this.chatOptions = chatOptions;
            return this;
        }

        public Builder inputKey(String inputKey) {
            this.inputKey = inputKey;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder sysParams(Map<String, Object> sysParams) {
            this.sysParams = sysParams;
            return this;
        }

        public Builder userPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
            return this;
        }

        public Builder userParams(Map<String, Object> userParams) {
            this.userParams = userParams;
            return this;
        }

        public Builder outputSchema(String outputSchema) {
            this.outputSchema = outputSchema;
            return this;
        }

        public Builder outputPackage(String outputPackage) {
            this.outputPackage = outputPackage;
            return this;
        }

        public LLMNode build() {
            return new LLMNode(this);
        }
    }

}