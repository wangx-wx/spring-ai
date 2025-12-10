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
@JsonClassDescription("商家交易订单收入请求参数")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class MerchantOrderIncomeTimeRequest extends BaseIncomeRequest {

    @JsonProperty(value = "equipmentType")
    @JsonPropertyDescription("设备类型")
    private String equipmentType;

    @JsonProperty(value = "groupName")
    @JsonPropertyDescription("场地/店铺名称")
    private String groupName;

    @JsonProperty(value = "equipmentValue")
    @JsonPropertyDescription("设备编号")
    private String equipmentValue;

    @JsonProperty(value = "city")
    @JsonPropertyDescription("城市")
    private String city;

    @JsonProperty(value = "province")
    @JsonPropertyDescription("省份")
    private String province;

    @JsonProperty(value = "district")
    @JsonPropertyDescription("区县")
    private String district;

    @JsonProperty(value = "industry")
    @JsonPropertyDescription("行业, 包括两轮车，充电，娱乐，按摩，洗水，零售")
    private String industry;
}