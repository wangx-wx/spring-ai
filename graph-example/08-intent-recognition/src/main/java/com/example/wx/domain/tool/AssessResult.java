package com.example.wx.domain.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/21 15:51
 */
public record AssessResult(
        @JsonProperty(required = true, value = "confidence")
        @JsonPropertyDescription("置信度评分，区间：[0,1]")
        String confidence,
        @JsonProperty(required = true, value = "status")
        @JsonPropertyDescription("状态判定，1=需要用户确认;2=意图匹配确认")
        String status,
        @JsonProperty(value = "reply")
        @JsonPropertyDescription("回复用户的信息，需要让用户确认的信息")
        String reply
) {
        public static AssessResult empty() {
                return new AssessResult("0", "1", "");
        }
}
