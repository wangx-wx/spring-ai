package com.example.wx.conf;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.node.AnswerNode;
import com.alibaba.cloud.ai.graph.node.AssignerNode;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author wangx
 * @description 子图定义
 * @create 2025/9/1 21:24
 */
public class TodoSubGraphFactory {
    public static CompiledGraph build(ChatClient chatClient) throws Exception {
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> map = new HashMap<>();
            map.put("task_content", new ReplaceStrategy());
            map.put("todo_desc", new ReplaceStrategy());
            map.put("created_task", new ReplaceStrategy());
            return map;
        };
        StateGraph subGraph = new StateGraph("create-todo-subgraph", keyStrategyFactory);

        // llm 润色用户输入
        subGraph.addNode("llm", node_async(state -> {
            LlmNode node = LlmNode.builder()
                    .userPromptTemplate("请直接用一句话帮我润色成待办事项描述，原内容为: {task_content}，不需要任何解释或格式，只回复润色后的内容。")
                    .params(Map.of("task_content", "null"))
                    .outputKey("todo_desc")
                    .chatClient(chatClient)
                    .build();
            return node.apply(state);
        }));

        // 合并变量 - 可用单例 AssignerNode
        AssignerNode assignerNode = AssignerNode.builder()
                .addItem("created_task", "todo_desc", AssignerNode.WriteMode.OVER_WRITE)
                .build();

        subGraph.addNode("assign", node_async(assignerNode));

        // 回答确认 - 可选
        AnswerNode answerNode = AnswerNode.builder()
                .answer("已创建任务：{todo_desc}")
                .build();
        subGraph.addNode("answer", node_async(answerNode));

        subGraph.addEdge(StateGraph.START, "llm");
        subGraph.addEdge("llm", "assign");
        subGraph.addEdge("assign", "answer");
        subGraph.addEdge("answer", StateGraph.END);

        return subGraph.compile();
    }
}
