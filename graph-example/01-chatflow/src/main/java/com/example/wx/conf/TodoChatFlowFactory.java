package com.example.wx.conf;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.node.AnswerNode;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.node.QuestionClassifierNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author wangxiang
 * @description
 * @create 2025/8/31 16:39
 */
public class TodoChatFlowFactory {

    public static CompiledGraph build(ChatClient chatClient, CompiledGraph subGraph) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> {
            Map<String, KeyStrategy> map = new HashMap<>();
            map.put("session_id", new ReplaceStrategy());
            map.put("user_input", new ReplaceStrategy());
            map.put("intent_type", new ReplaceStrategy());
            map.put("chat_reply", new ReplaceStrategy());
            map.put("tasks", new ReplaceStrategy());
            map.put("created_task", new ReplaceStrategy());
            map.put("answer", new ReplaceStrategy());
            return map;
        };

        StateGraph mainGraph = new StateGraph("chatFlow-demo", keyStrategyFactory);

        // 闲聊/多轮通用LLM - Lambda 动态 new llmNode
        mainGraph.addNode("chat", node_async(state -> {
            LlmNode node = LlmNode.builder()
                    .userPromptTemplate("{user_input}")
                    .params(Map.of("user_input", "null"))
                    .outputKey("chat_reply")
                    .chatClient(chatClient)
                    .build();
            return node.apply(state);
        }));

        // 问题分类节点
        QuestionClassifierNode intentClassifier = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("user_input")
                .categories(List.of("创建待办", "其它"))
                .classificationInstructions(List.of("判断用户是否想创建一个待办事项。如果是，返回'创建待办'，否则返回'其它'"))
                .outputKey("intent_type")
                .build();
        mainGraph.addNode("intent", node_async(intentClassifier));

        // 调用子图节点
        NodeAction callSubGraphNode = (OverAllState state) -> {
            String mainThreadId = (String) state.value("session_id").orElse("user-001");
            String subThreadId = mainThreadId + "-todo-" + UUID.randomUUID();
            String userInput = (String) state.value("user_input").orElse("");
            // 提取待办内容
            String taskContent = userInput;
            int idx = userInput.indexOf("：");
            if (idx > 0 && idx + 1 < userInput.length()) {
                taskContent = userInput.substring(idx + 1).trim();
            }
            Map<String, Object> input = Map.of("task_content", taskContent);

            var subResult = subGraph.invoke(input, RunnableConfig.builder().threadId(subThreadId).build());
            if (subResult.isPresent()) {
                Object createdTaskObj = subResult.get().value("created_task").orElse(null);
                String createdTask = null;
                if (createdTaskObj instanceof String s) {
                    createdTask = s;
                } else if (createdTaskObj instanceof AssistantMessage am) {
                    createdTask = am.getText();
                } else if (createdTaskObj != null) {
                    createdTask = createdTaskObj.toString();
                }
                List<String> tasks = (List<String>) state.value("tasks").orElse(new ArrayList<>());
                tasks = new ArrayList<>(tasks);
                if (createdTask != null && !createdTask.isBlank()) {
                    tasks.add(createdTask);
                }
                return Map.of("tasks", tasks, "created_task", createdTask);
            }
            return Map.of();
        };
        mainGraph.addNode("callSubGraph", node_async(callSubGraphNode));

        // 主流程答复节点
        AnswerNode mainReply = AnswerNode.builder()
                .answer("你当前待办有：{{tasks}}\n闲聊回复：{{chat_reply}}")
                .build();
        mainGraph.addNode("mainReply", node_async(mainReply));

        mainGraph.addEdge(StateGraph.START, "intent");
        // intent_type判定：如果为"创建待办"则进入子图，否则普通闲聊
        mainGraph.addConditionalEdges("intent", edge_async(state -> {
            String intentRaw = (String) state.value("intent_type").orElse("");
            String intent = intentRaw;
            try {
                // 去除 markdown code block
                intentRaw = intentRaw.trim();
                if (intentRaw.startsWith("```json")) {
                    intentRaw = intentRaw.replaceFirst("^```json", "").trim();
                }
                if (intentRaw.startsWith("```")) {
                    intentRaw = intentRaw.replaceFirst("^```", "").trim();
                }
                if (intentRaw.endsWith("```")) {
                    intentRaw = intentRaw.replaceAll("```$", "").trim();
                }
                // 解析 JSON
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(intentRaw);
                if (node.has("category_name")) {
                    intent = node.get("category_name").asText();
                }
            } catch (Exception e) {

            }
            return "创建待办".equals(intent) ? "callSubGraph" : "chat";
        }), Map.of("callSubGraph", "callSubGraph", "chat", "chat"));

        mainGraph.addEdge("callSubGraph", "mainReply");
        mainGraph.addEdge("chat", "mainReply");
        mainGraph.addEdge("mainReply", StateGraph.END);

        return mainGraph.compile();
    }
}
