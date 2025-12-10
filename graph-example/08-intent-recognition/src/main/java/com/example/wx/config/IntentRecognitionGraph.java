package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.config.node.LLMNode;
import com.example.wx.config.node.RagNode;
import com.example.wx.domain.LLMConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/10 15:24
 */
@RequiredArgsConstructor
@Configuration
@Slf4j
public class IntentRecognitionGraph {


    @Value("classpath:/prompts/rewrite_prompt.st")
    private Resource rewritePrompt;

    @Value("classpath:/prompts/intent_prompt.st")
    private Resource intentPrompt;

    @Bean
    public StateGraph stateGraphIntentRecognition(ChatModel chatModel,
            DashScopeDocumentRetriever dashScopeDocumentRetriever) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactoryBuilder = new KeyStrategyFactoryBuilder()
                .addPatternStrategy("history_list", new AppendStrategy())
                .addPatternStrategy("user_query", new ReplaceStrategy())
                .addPatternStrategy("rewrite_query", new ReplaceStrategy())
                .build();

        // 问题重写节点
        var rewriteNode = new LLMNode(chatModel, LLMConfig.builder()
                .topP(0.7)
                .systemPrompt(resourceToString(rewritePrompt))
                .temperature(0.5)
                .params(new HashMap<>(Map.of("history_list", List.of())))
                .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                .queryKey("user_query")
                .outputKey("rewrite_query")
                .build());

        // 语义召回节点
        var ragNode = new RagNode("rag_list", "rewrite_query", dashScopeDocumentRetriever);

        // 意图识别节点
        var intentNode = new LLMNode(chatModel, LLMConfig.builder()
                .topP(0.7)
                .systemPrompt(resourceToString(intentPrompt))
                .temperature(0.5)
                .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                .queryKey("user_query")
                .params(new HashMap<>(Map.of("nowDate", "", "weekDay", "", "weekOfYear", "")))
                .outputKey("intent_result")
                .build());

        // 槽位提取
        return null;
    }


    private String resourceToString(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read resource", ex);
        }
    }

}
