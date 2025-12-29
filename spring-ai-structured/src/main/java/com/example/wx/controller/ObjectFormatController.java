package com.example.wx.controller;

import com.example.wx.domain.ObjectEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 17:25
 */
@RestController
@RequestMapping("/object")
public class ObjectFormatController {
    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final BeanOutputConverter<ObjectEntity> converter;
    private final String format;
    private static final Logger log = LoggerFactory.getLogger(ObjectFormatController.class);

    public ObjectFormatController(ChatClient.Builder builder, ChatModel chatModel) {
        this.chatModel = chatModel;

        this.converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<ObjectEntity>() {
                }
        );
        this.format = converter.getFormat();
        log.info("format: {}", format);
        this.chatClient = builder
                .build();
    }


    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "以影子为作者，写一篇200字左右的有关人工智能诗篇") String query) {
        String result = chatClient.prompt(query)
                .call().content();

        log.info("result: {}", result);
        assert result != null;
        try {
            ObjectEntity convert = converter.convert(result);
            log.info("反序列成功，convert: {}", convert);
        } catch (Exception e) {
            log.error("反序列化失败");
        }
        return result;
    }


    @GetMapping("/chat-format")
    public String simpleChatFormat(@RequestParam(value = "query", defaultValue = "以影子为作者，写一篇200字左右的有关人工智能诗篇") String query) {
        String promptUserSpec = """
                format: 以纯文本输出 json，请不要包含任何多余的文字——包括 markdown 格式;
                outputExample: {format};
                """;
        String result = chatClient.prompt(query)
                .user(u -> u.text(promptUserSpec)
                        .param("format", format))
                .call().content();

        log.info("result: {}", result);
        assert result != null;
        try {
            ObjectEntity convert = converter.convert(result);
            log.info("反序列成功，convert: {}", convert);
        } catch (Exception e) {
            log.error("反序列化失败");
        }
        return result;
    }
}
