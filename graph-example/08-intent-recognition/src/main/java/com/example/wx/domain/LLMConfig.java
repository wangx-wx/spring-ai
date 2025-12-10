package com.example.wx.domain;

import static com.example.wx.constants.IntentGraphParams.USER_QUERY;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/9 22:16
 */
@Data
@Builder
public class LLMConfig {

    @Builder.Default
    private String queryKey = USER_QUERY;

    @Builder.Default
    private String outputKey = null;

    @Builder.Default
    private Map<String, Object> sysParams = new HashMap<>();

    @Builder.Default
    private String systemPrompt = "you are a helpful assistant";

    /**
     * user 消息模板，为 null 时使用 queryKey 从 state 获取纯文本
     * 设置后支持 ST 模板语法，如 "用户问题: {user_query}\nRAG结果: {rag_list}"
     */
    @Builder.Default
    private String userPrompt = null;

    /**
     * user 消息模板的变量 keys，为 null 时不进行模板替换
     * key 为模板变量名，value 为占位符（运行时从 state 获取实际值）
     */
    @Builder.Default
    private Map<String, Object> userParams = null;

    @Builder.Default
    private String model = "qwen-max";

    @Builder.Default
    private Double temperature = 0.5;

    @Builder.Default
    private Double topP = 0.7;

    @Builder.Default
    private boolean includeMetadata = false;

    @Builder.Default
    private boolean structuredOutput = false;

    /**
     * 输出类型，为 null 时返回纯文本 String
     * 设置后会使用 ChatClient.entity() 进行结构化输出
     */
    @Builder.Default
    private Class<?> outputType = null;

    /**
     * 动态输出类型的状态键，从 state 中获取类的全限定名字符串
     * 优先级高于 outputType，设置后会通过 Class.forName() 加载类
     * 示例：state 中存储 "com.example.wx.domain.IntentResult"
     */
    @Builder.Default
    private String outputTypeKey = null;
}
