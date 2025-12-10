package com.example.wx.domain.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("下载商家收益请求参数")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class DownloadMerchantIncomeRequest extends BaseToolRequest {

    @JsonProperty(required = true, value = "startDate")
    @JsonPropertyDescription("查询的开始日期, 格式 yyyy-MM-dd")
    private String startDate = "2025-03-01";

    @JsonProperty(required = true, value = "endDate")
    @JsonPropertyDescription("查询的结束日期, 格式 yyyy-MM-dd")
    private String endDate = "2025-03-05";

    @JsonProperty(required = true, value = "email")
    @JsonPropertyDescription("接收邮箱地址")
    private String email;
}