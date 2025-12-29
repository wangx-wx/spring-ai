package com.example.wx.tools;

import java.util.function.Function;

/**
 * @author wangx
 * @description
 * @create 2025/6/27 16:09
 */
public class FunctionTools implements Function<FunctionTools.WeatherRequest, FunctionTools.WeatherResponse> {
    public WeatherResponse apply(WeatherRequest request) {
        System.out.println("request = " + request);
        return new WeatherResponse(30.0, Unit.C);
    }
    public enum Unit { C, F }
    public record WeatherRequest(String location, Unit unit) {}
    public record WeatherResponse(double temp, Unit unit) {}
}

