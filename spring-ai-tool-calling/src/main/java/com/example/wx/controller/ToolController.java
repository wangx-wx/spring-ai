package com.example.wx.controller;

import com.example.wx.compontent.DateTimeTools;
import com.example.wx.tools.DeclarativeTools;
import com.example.wx.tools.FunctionTools;
import com.example.wx.tools.ProgrammaticTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

/**
 * @author wangx
 * @description
 * @create 2025/6/27 10:55
 */
@RestController
public class ToolController {

    private final ChatClient chatClient;

    public ToolController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @GetMapping("/declarative")
    public String declarative(@RequestParam(value = "prompt", defaultValue = "现在的时间") String prompt) {
        ToolCallback[] from = ToolCallbacks.from(new DeclarativeTools());
        return chatClient.prompt(prompt).toolCallbacks(from).call().content();
    }

    @GetMapping("/programmatic")
    public String programmatic(@RequestParam(value = "prompt", defaultValue = "现在的时间") String prompt) {
        Method method = ReflectionUtils.findMethod(ProgrammaticTools.class, "getTime");
        ToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .description("Get the current date and time in the user's timezone")
                        .name("getTime")
                        .inputSchema("""
                                {
                                    "type": "object",
                                    "properties": {
                                        "location": {
                                            "type": "format"
                                        },
                                        "unit": {
                                            "type": "string",
                                            "enum": ["C", "F"]
                                        }
                                    },
                                    "required": ["format", "unit"]
                                }
                                """)
                        .build())
                .toolMethod(method)
                .toolObject(new ProgrammaticTools())
                .build();
        return chatClient.prompt(prompt).toolCallbacks(toolCallback).call().content();
    }

    @GetMapping("/weather")
    public String weather(@RequestParam(value = "prompt", defaultValue = "现在广州天气如何，使用摄氏度。") String prompt) {
        ToolCallback toolCallback = FunctionToolCallback
                .builder("currentWeather", new FunctionTools())
                .description("Get the weather")
                .inputType(FunctionTools.WeatherRequest.class)
                .build();
        return chatClient.prompt(prompt).toolCallbacks(toolCallback).call().content();
    }

    @GetMapping("/weather2")
    public String weather2(@RequestParam(value = "prompt", defaultValue = "现在广州天气如何，使用摄氏度。") String prompt) {
        return chatClient.prompt(prompt).toolNames("getWeather").call().content();
    }


}
