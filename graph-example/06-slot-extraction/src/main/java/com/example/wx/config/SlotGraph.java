package com.example.wx.config;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.dto.Result;
import com.example.wx.node.InterruptableNodeAction;
import com.example.wx.node.SlotExtractionNode;
import com.example.wx.node.SlotNode;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author wangx
 * @description
 * @create 2025/12/7 21:47
 */
@Configuration
public class SlotGraph {
    @Bean
    public StateGraph slotAnalysisGraph(ChatModel chatModel) throws GraphStateException {


        var step1 = node_async(state -> {
            return Map.of("messages", "Step 1");
        });

        // var slotNode = new SlotNode(chatModel);
        // var humanFeedback = new InterruptableNodeAction("human_feedback", "等待用户输入");

        var slotExtractionNode = new SlotExtractionNode(chatModel);

        var step3 = node_async(state -> {
            return Map.of("messages", "Step 3");
        });

        // 定义条件边：根据 human_feedback 的值决定路由
        var evalHumanFeedback = edge_async(state -> {
            var slotParams = state.value("slot_params", Result.class).get();
            return "1".equals(slotParams.status()) ? "back" : "next";
        });

        // 配置 KeyStrategyFactory
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("messages", new AppendStrategy());
            keyStrategyHashMap.put("user_query", new ReplaceStrategy());
            keyStrategyHashMap.put("nowData", new ReplaceStrategy());
            keyStrategyHashMap.put("nowDay", new ReplaceStrategy());
            keyStrategyHashMap.put("history", new ReplaceStrategy());
            keyStrategyHashMap.put("slot_params", new ReplaceStrategy());
            return keyStrategyHashMap;
        };

        StateGraph graph = new StateGraph(keyStrategyFactory);

        // graph.addNode("slot", node_async(slotNode))
        //         .addNode("human_feedback", humanFeedback)
        //         .addNode("step_3", step3)
        //         .addNode("step_1", step1)
        //         .addEdge(START, "step_1")
        //         .addEdge("step_1", "slot")
        //         .addEdge("slot", "human_feedback")
        //         .addConditionalEdges("human_feedback", evalHumanFeedback, Map.of("back", "slot", "next", "step_3"))
        //         .addEdge("step_3", END);
        graph.addNode("slot", slotExtractionNode)
                .addNode("step_3", step3)
                .addNode("step_1", step1)
                .addEdge(START, "step_1")
                .addEdge("step_1", "slot")
                .addConditionalEdges("slot", evalHumanFeedback, Map.of("back", "slot", "next", "step_3"))
                .addEdge("step_3", END);
        GraphRepresentation representation = graph.getGraph(GraphRepresentation.Type.MERMAID, "Product Analysis Graph");
        System.out.println("\n=== Product Analysis Graph UML Flow ===");
        System.out.println(representation.content());
        System.out.println("======================================\n");
        return graph;
    }
}
