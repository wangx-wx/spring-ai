package com.example.wx.config;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.dispatcher.FeedbackDispatcher;
import com.example.wx.node.RewordingNode;
import com.example.wx.node.SummarizerNode;
import com.example.wx.node.SummaryFeedbackClassifierNode;
import com.example.wx.node.TitleGeneratorNode;
import java.util.HashMap;
import java.util.Map;
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
 * @create 2025/9/29 16:18
 */
@Configuration
public class WritingAssistantConfig {

    private final static Logger logger = LoggerFactory.getLogger(WritingAssistantConfig.class);

    @Bean
    public StateGraph writingAssistantGraph(ChatModel chatModel) throws GraphStateException {
        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();

        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> keyStrategies = new HashMap<>();
            keyStrategies.put("original_text", new ReplaceStrategy());
            keyStrategies.put("summary", new ReplaceStrategy());
            keyStrategies.put("summary_feedback", new ReplaceStrategy());
            keyStrategies.put("reworded", new ReplaceStrategy());
            keyStrategies.put("title", new ReplaceStrategy());
            return keyStrategies;
        };

        // 添加节点
        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("summarizer", node_async(new SummarizerNode(chatClient)))
                .addNode("feedback_classifier", node_async(new SummaryFeedbackClassifierNode(chatClient, "summary")))
                .addNode("reworder", node_async(new RewordingNode(chatClient)))
                .addNode("title_generator", node_async(new TitleGeneratorNode(chatClient)));

        // 节点连接
        stateGraph.addEdge(START, "summarizer")
                .addEdge("summarizer", "feedback_classifier")
                .addConditionalEdges("feedback_classifier", edge_async(new FeedbackDispatcher()),
                        Map.of("positive", "reworder", "negative", "summarizer"))
                .addEdge("reworder", "title_generator")
                .addEdge("title_generator", END);

        logger.info("WritingAssistantGraph PlantUML 打印开始");
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "writing assistant flow");

        logger.info("=== Writing Assistant UML Flow ===");
        logger.info(representation.content());

        return stateGraph;
    }


}
