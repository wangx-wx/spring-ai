package com.example.wx.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
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
     * 事件类型：text(文本) / option(选项) / end(结束) / error(错误)
     */
    @JsonProperty("type")
    private String type;

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

    /**
     * 选项列表（type=option时使用）
     */
    @JsonProperty("options")
    private List<Option> options;

    /**
     * 选项配置，用于用户交互式选择
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option {
        /**
         * 选项编码，用于客户端回调标识
         */
        private String code;

        /**
         * 选项显示值
         */
        private String value;

        /**
         * 选项描述（可选）
         */
        private String description;

        /**
         * 是否为默认选项
         */
        private Boolean isDefault;

        public Option(String code, String value) {
            this.code = code;
            this.value = value;
            this.isDefault = false;
        }

        public Option(String code, String value, String description) {
            this.code = code;
            this.value = value;
            this.description = description;
            this.isDefault = false;
        }
    }

    // ========== 工厂方法 ==========

    /**
     * 创建文本类型结果
     */
    public static ChatResult text(String content) {
        return ChatResult.builder()
                .type("text")
                .content(content)
                .build();
    }

    /**
     * 创建选项类型结果
     */
    public static ChatResult option(List<Option> options) {
        return ChatResult.builder()
                .type("option")
                .options(options)
                .build();
    }

    /**
     * 创建结束类型结果
     */
    public static ChatResult end() {
        return ChatResult.builder()
                .type("end")
                .content("[DONE]")
                .build();
    }

    /**
     * 创建错误类型结果
     */
    public static ChatResult error(String errorMessage) {
        return ChatResult.builder()
                .type("error")
                .content(errorMessage)
                .build();
    }
}
