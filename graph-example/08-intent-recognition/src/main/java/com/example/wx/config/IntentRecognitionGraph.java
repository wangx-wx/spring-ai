package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.config.node.LLMNode;
import com.example.wx.config.node.RagNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;
import static com.example.wx.constants.IntentGraphParams.CLARIFY_LIST;
import static com.example.wx.constants.IntentGraphParams.HISTORY;
import static com.example.wx.constants.IntentGraphParams.INTENT_RESULT;
import static com.example.wx.constants.IntentGraphParams.NOW_DATE;
import static com.example.wx.constants.IntentGraphParams.OUTPUT_SCHEMA_KEY;
import static com.example.wx.constants.IntentGraphParams.RAG_RESULT;
import static com.example.wx.constants.IntentGraphParams.REWRITE_QUERY;
import static com.example.wx.constants.IntentGraphParams.SLOT_PARAMS;
import static com.example.wx.constants.IntentGraphParams.USER_QUERY;
import static com.example.wx.constants.IntentGraphParams.WEEK_DAY;
import static com.example.wx.constants.IntentGraphParams.WEEK_OF_YEAR;

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
                .addPatternStrategy(HISTORY, new AppendStrategy())
                .addPatternStrategy(USER_QUERY, new ReplaceStrategy())
                .addPatternStrategy(REWRITE_QUERY, new ReplaceStrategy())
                .addPatternStrategy(INTENT_RESULT, new ReplaceStrategy())
                .addPatternStrategy(NOW_DATE, new ReplaceStrategy())
                .addPatternStrategy(WEEK_DAY, new ReplaceStrategy())
                .addPatternStrategy(WEEK_OF_YEAR, new ReplaceStrategy())
                .build();

        // 问题重写节点
        var rewriteNode = LLMNode.builder()
                .chatModel(chatModel)
                .systemPrompt(resourceToString(rewritePrompt))
                .sysParams(new HashMap<>(Map.of(HISTORY, List.of())))
                .inputKey(USER_QUERY)
                .outputKey(REWRITE_QUERY)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .build();

        // 语义召回节点
        var ragNode = new RagNode(REWRITE_QUERY, RAG_RESULT, dashScopeDocumentRetriever);

        // 意图识别节点
        LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .systemPrompt(resourceToString(intentPrompt))
                .userPrompt("")
                .userParams(new HashMap<>(Map.of(USER_QUERY, "", REWRITE_QUERY, "", RAG_RESULT, "")))
                .outputKey(INTENT_RESULT)
                .build();

        // 槽位提取
        var slotNode = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .systemPrompt("")
                .outputKey(SLOT_PARAMS)
                .outputSchemaKey(OUTPUT_SCHEMA_KEY)
                .userPrompt("")
                .userParams(new HashMap<>(Map.of(USER_QUERY, "", CLARIFY_LIST, "")))
                .outputSchemaKey("output")
                .build();

        // 知识库问答节点
        var knowledgeNode = node_async(state -> {
            // todo 知识库问答
            return Map.of();
        });

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
