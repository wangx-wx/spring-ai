package com.example.wx.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SSE聊天结果传输对象
 * 可直接通过 Flux<ChatResult> 返回，Spring会自动序列化为SSE格式
 *
 * @author wangx
 * @create 2026/1/9 23:48
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResult {
    /**
     * 节点名称
     */
    @JsonProperty("node_name")
    private String nodeName;

    /**
     * 文本内容
     */
    @JsonProperty("content")
    private String content;


    // ========== 工厂方法 ==========

    /**
     * 创建文本类型结果
     */
    public static ChatResult text(String content) {
        return ChatResult.builder()
                .content(content)
                .build();
    }

    /**
     * 创建结束类型结果
     */
    public static ChatResult end() {
        return ChatResult.builder()
                .content("[DONE]")
                .build();
    }

    /**
     * 创建错误类型结果
     */
    public static ChatResult error(String errorMessage) {
        return ChatResult.builder()
                .content(errorMessage)
                .build();
    }
}
