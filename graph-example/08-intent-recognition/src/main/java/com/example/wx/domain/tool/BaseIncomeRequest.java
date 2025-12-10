package com.example.wx.domain.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class BaseIncomeRequest extends BaseToolRequest {

    @JsonProperty(required = true, value = "timeSta")
    @JsonPropertyDescription("格式 yyyy-MM-dd，用户询问时间范围的开始日期, 如查询205年2月1～2025年2月3，则传2025-02-01，如查询2025年3月，则传2025-03-01，如查询2025年4～6月则传2025-04-01")
    private String timeSta;

    @JsonProperty(required = true, value = "timeEnd")
    @JsonPropertyDescription("格式 yyyy-MM-dd，用户询问时间范围的结束日期, 如查询2025年2月1～2025年2月3，则传2025-02-03，如查询2025年3月，则传2025-03-31，如查询2025年4～6月则传2025-06-30")
    private String timeEnd;

    @JsonProperty(required = true, value = "latitude")
    @JsonPropertyDescription("日期查询维度, 包括 1:日维度，2:周维度, 3:月维度, 4:年维度")
    private Integer latitude;
}