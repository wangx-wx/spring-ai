package com.example.wx;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * @author wangx
 * @description
 * @create 2025/7/12 21:01
 */
@SpringBootApplication
public class NacosMcpClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(NacosMcpClientApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ChatModel chatModel, @Qualifier("distributedAsyncToolCallback") ToolCallbackProvider tools,
                                               ConfigurableApplicationContext context) {
        ToolCallback[] toolCallbacks = tools.getToolCallbacks();
        System.out.println(">>> Available tools: ");
        for (int i = 0; i < toolCallbacks.length; i++) {
            System.out.println("[" + i + "] " + toolCallbacks[i].getToolDefinition().name());
        }
        return args -> {
            var chatClient = ChatClient.builder(chatModel)
                    .defaultToolCallbacks(tools.getToolCallbacks())
                    .build();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\n>>> QUESTION: ");
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                if (userInput.isEmpty()) {
                    userInput = "北京时间现在几点钟";
                }
                System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
            }
            scanner.close();
            context.close();
        };
    }
}