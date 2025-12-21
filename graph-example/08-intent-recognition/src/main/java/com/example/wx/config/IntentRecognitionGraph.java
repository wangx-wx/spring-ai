package com.example.wx.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.config.node.AgentToolWaitNode;
import com.example.wx.config.node.AssessWaitNode;
import com.example.wx.config.node.LLMNode;
import com.example.wx.config.node.RagNode;
import com.example.wx.config.node.WaitNode;
import com.example.wx.domain.tool.AssessResult;
import com.example.wx.domain.tool.SlotFillingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
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

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;
import static com.example.wx.constants.IntentGraphParams.AGENT_TOOL_INPUT;
import static com.example.wx.constants.IntentGraphParams.AGENT_TOOL_OUTPUT;
import static com.example.wx.constants.IntentGraphParams.ASSESS_RESULT;
import static com.example.wx.constants.IntentGraphParams.CLARIFY_LIST;
import static com.example.wx.constants.IntentGraphParams.HISTORY;
import static com.example.wx.constants.IntentGraphParams.INTENT_DESC;
import static com.example.wx.constants.IntentGraphParams.INTENT_RAG_RESULT;
import static com.example.wx.constants.IntentGraphParams.INTENT_RESULT;
import static com.example.wx.constants.IntentGraphParams.NOW_DATE;
import static com.example.wx.constants.IntentGraphParams.OUTPUT_SCHEMA_KEY;
import static com.example.wx.constants.IntentGraphParams.QA_RAG_RESULT;
import static com.example.wx.constants.IntentGraphParams.REPLY;
import static com.example.wx.constants.IntentGraphParams.REWRITE_QUERY;
import static com.example.wx.constants.IntentGraphParams.USER_QUERY;
import static com.example.wx.constants.IntentGraphParams.WEEK_DAY;
import static com.example.wx.constants.IntentGraphParams.WEEK_OF_YEAR;
import static com.example.wx.constants.IntentParamConstants.INTENT_DESC_MAP;
import static com.example.wx.constants.IntentParamConstants.INTENT_MAP;
import static com.example.wx.constants.PromptConstant.INTENT_NODE_USER_PROMPT;

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

    @Value("classpath:/prompts/assess_prompt.st")
    private Resource assessPrompt;

    @Bean
    public StateGraph stateGraphIntentRecognition(ChatModel chatModel,
                                                  DashScopeDocumentRetriever intentKnowledgeRetriever,
                                                  DashScopeDocumentRetriever qaKnowledgeRetriever) throws GraphStateException {

        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                .addPatternStrategy(HISTORY, new AppendStrategy())
                .addPatternStrategy(USER_QUERY, new ReplaceStrategy())
                .addPatternStrategy(REWRITE_QUERY, new ReplaceStrategy())
                .addPatternStrategy(INTENT_RESULT, new ReplaceStrategy())
                .addPatternStrategy(INTENT_RAG_RESULT, new ReplaceStrategy())
                .addPatternStrategy(QA_RAG_RESULT, new ReplaceStrategy())
                .addPatternStrategy(NOW_DATE, new ReplaceStrategy())
                .addPatternStrategy(WEEK_DAY, new ReplaceStrategy())
                .addPatternStrategy(WEEK_OF_YEAR, new ReplaceStrategy())
                .addPatternStrategy(CLARIFY_LIST, new AppendStrategy())
                .addPatternStrategy(OUTPUT_SCHEMA_KEY, new ReplaceStrategy())
                .addPatternStrategy(REPLY, new ReplaceStrategy())
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
        var intentRagNode = new RagNode(USER_QUERY, INTENT_RAG_RESULT, intentKnowledgeRetriever);

        // 意图识别节点
        var intentNode = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .systemPrompt(resourceToString(intentPrompt))
                .userPrompt(INTENT_NODE_USER_PROMPT)
                .userParams(new HashMap<>(Map.of(USER_QUERY, "", REWRITE_QUERY, "", INTENT_RAG_RESULT, "")))
                .outputKey(INTENT_RESULT)
                .build();

        NodeAction setParamNode = (OverAllState state) -> {
            var intentResult = state.value(INTENT_RESULT, String.class).orElse("商家维度经营分析");
            var intentDesc = INTENT_DESC_MAP.get(intentResult);
            return Map.of(INTENT_DESC, intentDesc);
        };

        // 意图评估
        // 使用其他模型判断用户输入是否属于这个意图
        // 如果置信度比较低，需要用户确认
        var assessSchema = new BeanOutputConverter<>(AssessResult.class);
        var assessIntent = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .systemPrompt(resourceToString(assessPrompt))
                .sysParams(new HashMap<>(Map.of(HISTORY, "", USER_QUERY, "", INTENT_RAG_RESULT, (Object) null,
                        INTENT_RESULT, "", INTENT_DESC, "")))
                .outputKey(ASSESS_RESULT)
                .outputSchema(assessSchema.getFormat())
                .build();

        var assessWaitNode = new AssessWaitNode(AGENT_TOOL_OUTPUT, REPLY);

        var agentToolWaitNode = new AgentToolWaitNode(AGENT_TOOL_OUTPUT, REPLY);
        var agentNode = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder().build())
                .build();

        var edged = edge_async(state -> {
            var slotParams = state.value("slot_params", SlotFillingResult.class).orElse(SlotFillingResult.empty());
            return "1".equals(slotParams.status()) ? "back" : "next";
        });

        // 知识库问答节点
        var qaRagNode = new RagNode(USER_QUERY, QA_RAG_RESULT, qaKnowledgeRetriever);

        var qaNode = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .systemPrompt("")
                .sysParams(new HashMap<>(Map.of(QA_RAG_RESULT, "")))
                .inputKey(USER_QUERY)
                .outputKey(REPLY)
                .build();

        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("_rewrite_node_", node_async(rewriteNode))
                .addNode("_intent_rag_node_", node_async(intentRagNode))
                .addNode("_intent_node_", node_async(intentNode))
                // .addNode("_slot_node_", node_async(slotNode))
                // .addNode("_slot_wait_node_", slotWaitNode)
                .addNode("_qa_rag_node_", node_async(qaRagNode))
                .addNode("_qa_node_", node_async(qaNode))
                .addNode("_agent_node_", node_async(agentNode))
                .addEdge(StateGraph.START, "_rewrite_node_")
                .addEdge("_rewrite_node_", "_intent_rag_node_")
                .addEdge("_intent_rag_node_", "_intent_node_")
                .addConditionalEdges("_intent_node_",
                        edge_async(state -> state.value(INTENT_RESULT, "其他场景")),
                        INTENT_MAP)
                .addEdge("_slot_node_", "_slot_wait_node_")
                .addConditionalEdges("_slot_wait_node_", edged, Map.of("back", "_slot_node_", "next", "_agent_node_"))
                .addEdge("_agent_node_", StateGraph.END)
                .addEdge("_qa_rag_node_", "_qa_node_")
                .addEdge("_qa_node_", StateGraph.END);
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "Issue Clarify Graph");
        System.out.println("======================================");
        System.out.println(representation.content());
        System.out.println("======================================");
        return stateGraph;
    }


    private String resourceToString(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read resource", ex);
        }
    }

}
