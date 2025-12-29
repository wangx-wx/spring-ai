package com.example.wx;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.example.wx.tools.SearchTool;
import com.example.wx.tools.WeatherTool;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Set;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author wangx
 * @description
 * @create 2025/12/25 23:04
 */
@SpringBootApplication
public class ReactAgentApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename(".env").load();
        Set<String> set = Set.of("DASH_SCOPE_API_KEY");
        for (String item : set) {
            System.setProperty(item, dotenv.get(item));
        }
        SpringApplication.run(ReactAgentApplication.class, args);
    }

//    @Bean
    CommandLineRunner commandLineRunner(ChatModel chatModel) {
        return args -> {
            // 创建模型实例
//            DashScopeApi dashScopeApi = DashScopeApi.builder()
//                    .apiKey(System.getenv("DASH_SCOPE_API_KEY"))
//                    .build();
//            ChatModel chatModel = DashScopeChatModel.builder()
//                    .dashScopeApi(dashScopeApi)
//                    .build();

            ToolCallback searchTool = FunctionToolCallback.builder("search", new SearchTool())
                    .description("搜索工具")
                    .inputType(String.class)
                    .build();
            ToolCallback weatherTool = FunctionToolCallback.builder("weather", new WeatherTool())
                    .inputType(String.class)
                    .description("天气查询工具").build();


            // 创建 Agent
            ReactAgent agent = ReactAgent.builder()
                    .name("service_agent")
                    .model(chatModel)
                    .instruction("You are a helpful weather forecast assistant.")
                    .returnReasoningContents(true)
                    .tools(searchTool, weatherTool)
                    .enableLogging(true)
                    .build();

            // 运行 Agent
            AssistantMessage call = agent.call("查询杭州天气并推荐活动");
            System.out.println("Hello World!");
            System.out.println("call = " + call);
        };
    }
}