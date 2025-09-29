package com.example.wx.config;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.GraphRepresentation.Type;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.node.InputNode;
import com.example.wx.node.KeywordExtractionNode;
import com.example.wx.node.MergeResultsNode;
import com.example.wx.node.SentimentAnalysisNode;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/29 16:55
 */
@Configuration
public class ParallelGraphConfig {

    private final static Logger logger = LoggerFactory.getLogger(ParallelGraphConfig.class);

    @Bean
    public StateGraph parallelGraph(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
        // 状态工厂注册字段与策略
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("inputText", new ReplaceStrategy());
            keyStrategyHashMap.put("sentiment", new ReplaceStrategy());
            keyStrategyHashMap.put("keywords", new ReplaceStrategy());
            keyStrategyHashMap.put("analysis", new ReplaceStrategy());
            return keyStrategyHashMap;
        };

        // 注册节点
        StateGraph stateGraph = new StateGraph("parallelDemo", keyStrategyFactory)
                .addNode("start", node_async(new InputNode()))
                .addNode("sentiment", node_async(new SentimentAnalysisNode(chatClient, "inputText")))
                .addNode("keyword", node_async(new KeywordExtractionNode(chatClient, "inputText")))
                .addNode("merge", node_async(new MergeResultsNode()));

        // 构建并行边: 使用单边携带多目标
        stateGraph.addEdge(START, "sentiment")
                .addEdge(START, "keyword")
                // 限制: sentiment/keyword 并行后必须合并到同一节点
                .addEdge("sentiment", "merge")
                .addEdge("keyword", "merge")
                .addEdge("merge", END);

        // 可视化
        GraphRepresentation representation = stateGraph.getGraph(Type.PLANTUML, "parallel demo flow");
        logger.info("=== Parallel Demo UML Flow ===");
        logger.info(representation.content());
        logger.info("==================================");

        return stateGraph;
    }

}
