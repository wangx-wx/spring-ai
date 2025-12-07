package com.example.wx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;


public record Result(@JsonProperty(required = true, value = "status")
                     @JsonPropertyDescription("状态，1=需要补充信息;2=信息完善") String status,
                     @JsonProperty(value = "reply")
                     @JsonPropertyDescription("回复用户的信息，让用户补充槽位信息") String reply,
                     @JsonProperty(required = true, value = "slots")
                     @JsonPropertyDescription("槽位信息") SlotParams slots) {
    public record SlotParams(@JsonProperty(required = true, value = "startDate")
                             @JsonPropertyDescription("查询的开始日期, 格式 yyyy-MM-dd") String startTime,
                             @JsonProperty(required = true, value = "endDate")
                             @JsonPropertyDescription("查询的结束日期, 格式 yyyy-MM-dd") String endTime,
                             @JsonProperty(required = true, value = "email")
                             @JsonPropertyDescription("接收邮箱地址") String email) {
    }
}