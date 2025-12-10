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
@JsonClassDescription("商家设备维度数据请求参数")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EquipmentIncomeRequest extends MerchantOrderIncomeTimeRequest{

    @JsonProperty( required = true, value = "orderDirection")
    @JsonPropertyDescription("排序方式，默认desc, 包括 desc:降序, asc:升序")
    private String orderDirection;

    @JsonProperty( required = true, value = "orderBy")
    @JsonPropertyDescription("排序字段，默认payAmount, 包括 payAmount:总收入, payCounts:订单数")
    private String orderBy;
}