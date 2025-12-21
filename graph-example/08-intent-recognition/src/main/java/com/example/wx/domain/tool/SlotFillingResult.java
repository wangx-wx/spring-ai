package com.example.wx.domain.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SlotFillingResult<T extends BaseToolRequest>(
        @JsonProperty(required = true, value = "status")
        @JsonPropertyDescription("状态，1=需要补充信息;2=信息完善")
        String status,
        @JsonProperty(value = "reply")
        @JsonPropertyDescription("回复用户的信息，让用户补充槽位信息")
        String reply,
        @JsonProperty(required = true, value = "slots")
        @JsonPropertyDescription("槽位信息")
        T slots
) {
        public static <T extends BaseToolRequest> SlotFillingResult<T> empty() {
                return new SlotFillingResult<>("1", "", null);
        }
}