package com.example.wx.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.function.BiFunction;
import org.springframework.ai.chat.model.ToolContext;

/**
 * @author wangx
 * @description
 * @create 2025/12/26 16:03
 */
public class SearchTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(@JsonProperty(value = "query") @JsonPropertyDescription("搜索信息") String query, ToolContext toolContext) {
        // 实现搜索逻辑
        return "搜索结果: " + query + "冬泳";
    }
}
