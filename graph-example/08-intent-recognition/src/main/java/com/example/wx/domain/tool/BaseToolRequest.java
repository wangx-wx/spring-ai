package com.example.wx.domain.tool;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * @author wangx
 * @description
 * @create 2025/12/10 18:11
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "classType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DownloadMerchantIncomeRequest.class, name = "downloadMerchantIncomeRequest"),
        @JsonSubTypes.Type(value = MerchantOrderIncomeTimeRequest.class, name = "merchantOrderTimeRequest"),
})
@Data
public class BaseToolRequest {

}