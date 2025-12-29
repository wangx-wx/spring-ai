package com.example.wx.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * @author wangx
 * @description
 * @create 2025/12/7 21:10
 */
public class SearchTool implements Function<SearchTool.Request, String> {

    private static final Logger log = LoggerFactory.getLogger(SearchTool.class);

    public record Request(String query) {}

    @Override
    public String apply(Request request) {
        log.info("Executing search for: {}", request.query());
        // 模拟搜索结果
        return "Search result: The weather is cold with a low of 13 degrees";
    }
}
