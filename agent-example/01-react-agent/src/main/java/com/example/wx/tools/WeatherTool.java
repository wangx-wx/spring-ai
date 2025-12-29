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
public class WeatherTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(@JsonProperty(value = "city") @JsonPropertyDescription("城市名称") String city, ToolContext toolContext) {
        // 实现搜索逻辑
        return city + "天气晴朗，52度";
    }
}
