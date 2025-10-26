package com.example.wx.node;

import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.SubGraphNode;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author wangxiang
 * @description
 * @create 2025/10/25 17:30
 */
public class SimpleSubGraph implements SubGraphNode {
    private final ChatClient chatClient;

    private StateGraph subGraph;

    public SimpleSubGraph(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.subGraph = createSubGraph();
    }

    private StateGraph createSubGraph() {
        try {
            // 创建内部节点子图
            ChatNode subNode1 = ChatNode.create("SubGraphNode1", "sub_input", "sub_output1", chatClient,
                    "Please preform the first step processing on the following content:");
            ChatNode subNode2 = ChatNode.create("SubGraphNode2", "sub_output1", "sub_output2", chatClient
                    , "Please preform the second step processing on the following content:");
            ChatNode subNode3 = ChatNode.create("SubGraphNode3", "sub_output2", "subgraph_final_output", chatClient
                    , "Please preform the final processing on the following content:");

            KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                    .addPatternStrategy("sub_input", new ReplaceStrategy())
                    .addPatternStrategy("sub_output1", new ReplaceStrategy())
                    .addPatternStrategy("sub_output2", new ReplaceStrategy())
                    .addPatternStrategy("subgraph_final_output", new ReplaceStrategy())
                    .addPatternStrategy("logs", new AppendStrategy())
                    .build();

            StateGraph stateGraph = new StateGraph("Simple Subgraph", keyStrategyFactory);

            stateGraph.addNode("sub_node1", node_async(subNode1))
                    .addNode("sub_node2", node_async(subNode2))
                    .addNode("sub_node3", node_async(subNode3))

                    .addEdge(StateGraph.START, "sub_node1")
                    .addEdge("sub_node1", "sub_node2")
                    .addEdge("sub_node2", "sub_node3")
                    .addEdge("sub_node3", StateGraph.END);

            return stateGraph;
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String id() {
        return "simple_subgraph";
    }

    @Override
    public StateGraph subGraph() {
        return this.subGraph;
    }
}
