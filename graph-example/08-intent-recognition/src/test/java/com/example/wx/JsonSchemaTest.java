package com.example.wx;

import com.example.wx.domain.tool.DownloadMerchantIncomeRequest;
import com.example.wx.domain.tool.MerchantOrderIncomeTimeRequest;
import com.example.wx.service.impl.ToolService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/20 22:42
 */
public class JsonSchemaTest {
    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> object = Class.forName("com.example.wx.domain.tool.MerchantOrderIncomeTimeRequest");
        Method allAnalyse = ReflectionUtils.findMethod(ToolService.class, "allAnalyse", object, ToolContext.class);
        String s = JsonSchemaGenerator.generateForMethodInput(allAnalyse);
        System.out.println("s = " + s);
        var outputConverter = new BeanOutputConverter<>(MerchantOrderIncomeTimeRequest.class);
        System.out.println("outputConverter = " + outputConverter.getFormat());
    }
}
