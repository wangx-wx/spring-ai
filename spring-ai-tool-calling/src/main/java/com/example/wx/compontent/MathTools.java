package com.example.wx.compontent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 21:17
 */
public class MathTools {

    public record Compute(@ToolParam(description = "第一个数字") double a,
                          @ToolParam(description = "第二个数字")double b,
                          @ToolParam(description = "操作符")String operator) {}

    @Tool(description = "简单计算器")
    public double compute(Compute compute) {
        return switch (compute.operator) {
            case "+" -> compute.a + compute.b;
            case "-" -> compute.a - compute.b;
            case "*" -> compute.a * compute.b;
            case "/" -> compute.a / compute.b;
            default -> throw new IllegalArgumentException("<UNK>");
        };
    }
}
