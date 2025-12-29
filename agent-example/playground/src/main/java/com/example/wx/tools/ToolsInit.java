package com.example.wx.tools;

import com.example.wx.config.ToolCallingConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * @author wangx
 * @description
 * @create 2025/8/24 17:44
 */
@Component
@RequiredArgsConstructor
public class ToolsInit {
    private final ToolCallingConfig config;
    private final RestClient.Builder restClientbuilder;
    private final ResponseErrorHandler responseErrorHandler;

    public List<ToolCallback> getTools() {
        return List.of(buildBaiduTranslateTools());
    }

    private ToolCallback buildBaiduTranslateTools() {
        BeanOutputConverter<BaiduTranslateTools.Request> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<BaiduTranslateTools.Request>() {
                });

        return FunctionToolCallback
                .builder(
                        "BaiduTranslateService",
                        new BaiduTranslateTools(config.getBaidu().getTranslate().getAk(), config.getBaidu().getTranslate().getSk(), restClientbuilder, responseErrorHandler)
                ).description("Baidu translation function for general text translation.")
                .inputSchema(converter.getJsonSchema())
                .inputType(BaiduTranslateTools.BaiduTranslateToolRequest.class)
                .toolMetadata(ToolMetadata.builder().returnDirect(false).build())
                .build();
    }
}
