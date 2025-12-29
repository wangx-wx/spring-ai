package com.example.wx.domain.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * @author wangx
 * @description
 * @create 2025/12/21 15:51
 */
public record AgentToolResult(
        @JsonProperty(required = true, value = "status")
        @JsonPropertyDescription("状态判定，1=需要补充工具参数;2=工具参数完整")
        String status,
        @JsonProperty(value = "reply")
        @JsonPropertyDescription("回复用户的信息")
        String reply
) {
    public static AgentToolResult empty() {
        return new AgentToolResult("1", "");
    }
}
