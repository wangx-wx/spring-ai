package com.example.wx.controller;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/7/5 16:51
 */
@RestController
@RequestMapping("/nacos")
public class PromptController {

    private static final Logger logger = LoggerFactory.getLogger(PromptController.class);
    private final ChatClient client;

    private final ConfigurablePromptTemplateFactory promptTemplateFactory;

    public PromptController(
            ChatModel chatModel,
            ConfigurablePromptTemplateFactory promptTemplateFactory
    ) {

        this.client = ChatClient.builder(chatModel).build();
        this.promptTemplateFactory = promptTemplateFactory;
    }

    @GetMapping("/books")
    public Flux<String> generateJoke(
            @RequestParam(value = "author", required = false, defaultValue = "鲁迅") String authorName,
            HttpServletResponse response
    ) {

        // 防止输出乱码
        response.setCharacterEncoding("UTF-8");

        // 使用 nacos 的 prompt tmpl 创建 prompt
        ConfigurablePromptTemplate template = promptTemplateFactory.create(
                "author",
                "please list the three most famous books by this {author}."
        );
        Prompt prompt = template.create(Map.of("author", authorName));
        logger.info("最终构建的 prompt 为：{}", prompt.getContents());

        return client.prompt(prompt)
                .stream()
                .content();
    }

    @GetMapping("/template")
    public String template(@RequestParam(value = "name", required = false, defaultValue = "summary-template") String name) {
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate(name);
        return template.render();
    }

    /**
     * 聊天问候接口
     * 从Nacos获取chat-greeting提示词模板，生成个性化问候语后调用大模型
     *
     * @param name     用户名称
     * @param response HttpServletResponse
     * @return 大模型生成的问候响应（流式返回）
     */
    @GetMapping("/greeting")
    public Flux<String> greeting(
            @RequestParam(value = "name", defaultValue = "wangx") String name,
            HttpServletResponse response
    ) {
        response.setCharacterEncoding("UTF-8");

        // 从Nacos获取chat-greeting提示词模板
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate("chat-greeting");

        // 渲染模板并创建 Prompt
        Prompt prompt = template.create(Map.of("name", name));
        logger.info("问候语 prompt: {}", prompt.getContents());

        return client.prompt(prompt)
                .stream()
                .content();
    }
}
