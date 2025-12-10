package com.example.wx.domain;

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
    private String queryKey = "user_query";

    @Builder.Default
    private String outputKey = "answer";

    @Builder.Default
    private Map<String, Object> params = new HashMap<>();

    @Builder.Default
    private String systemPrompt = "you are a helpful assistant";

    @Builder.Default
    private String model = "qwen-max";

    @Builder.Default
    private Double temperature = 0.7;

    @Builder.Default
    private Double topP = 0.8;

    @Builder.Default
    private boolean includeMetadata = false;
}
