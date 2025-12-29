package com.example.wx.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.wx.exceptions.AppException;
import com.example.wx.service.SummarizerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wangx
 * @description
 * @create 2025/7/26 22:03
 */
@Service
public class DocsSummarizerServiceImpl implements SummarizerService {
    private static final Logger logger = LoggerFactory.getLogger(DocsSummarizerServiceImpl.class);

    private final ChatClient chatClient;

    public DocsSummarizerServiceImpl(
            SimpleLoggerAdvisor simpleLoggerAdvisor,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor,
            @Qualifier("dashscopeChatModel") ChatModel chatModel,
            @Qualifier("summarizerPromptTemplate") PromptTemplate docsSummaryPromptTemplate
    ) {

        this.chatClient = ChatClient.builder(chatModel)
                .defaultOptions(
                        DashScopeChatOptions.builder().withModel("deepseek-r1").build()
                ).defaultSystem(
                        docsSummaryPromptTemplate.getTemplate()
                ).defaultAdvisors(
                        messageChatMemoryAdvisor,
                        simpleLoggerAdvisor
                ).build();
    }

    public Flux<String> summary(MultipartFile file, String url) {
        String text = getText(url, file);
        if (!StringUtils.hasText(text)) {
            return Flux.error(new AppException("Invalid file content"));
        }

        return chatClient.prompt()
                .user("Summarize the document")
                .user(text)
                .stream().content();
    }

    private String getText(String url, MultipartFile file) {
        if (Objects.nonNull(file)) {
            logger.debug("Reading file content form MultipartFile");
            List<Document> documents = new TikaDocumentReader(file.getResource()).get();
            return documents.stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.joining("\n\n"));
        }

        if (StringUtils.hasText(url)) {
            logger.debug("Reading file content form url");
            List<Document> documents = new TikaDocumentReader(url).get();
            return documents.stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.joining("\n\n"));
        }
        return "";
    }
}
