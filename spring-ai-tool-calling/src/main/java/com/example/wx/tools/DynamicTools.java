package com.example.wx.tools;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * @author wangx
 * @description
 * @create 2025/6/27 16:23
 */
@Configuration
public class DynamicTools {
    @Bean
    @Description("Get the weather in location")
    Function<WeatherRequest, WeatherResponse> getWeather() {
        return (request) -> {
            return  new WeatherResponse(37.9, Unit.C);
        };
    }

    record WeatherRequest(@ToolParam(description = "The name of a city or a country") String location, Unit unit) {
    }

    public enum Unit {C, F}

    public record WeatherResponse(double temp, Unit unit) {
    }
}
