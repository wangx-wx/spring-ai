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
import com.example.wx.domain.RagDoc;
import com.example.wx.domain.tool.AgentToolResult;
import com.example.wx.domain.tool.AssessResult;
import com.example.wx.domain.tool.DownloadMerchantIncomeRequest;
import com.example.wx.domain.tool.MerchantOrderIncomeTimeRequest;
import com.example.wx.service.impl.ToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import static com.example.wx.constants.IntentGraphParams.RESUME;
import static com.example.wx.constants.IntentGraphParams.REWRITE_QUERY;
import static com.example.wx.constants.IntentGraphParams.SKIP_ASSESS_FLAG;
import static com.example.wx.constants.IntentGraphParams.USER_QUERY;
import static com.example.wx.constants.IntentGraphParams.WEEK_DAY;
import static com.example.wx.constants.IntentGraphParams.WEEK_OF_YEAR;
import static com.example.wx.constants.IntentParamConstants.INTENT_DESC_MAP;
import static com.example.wx.constants.PromptConstant.INTENT_NODE_USER_PROMPT;

/**
 * @author wangx
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

    @Value("classpath:/prompts/income_analyze.st")
    private Resource incomeAnalyzePrompt;

    private final ToolService toolService;

    @Bean
    public StateGraph stateGraphIntentRecognition(ChatModel chatModel,
                                                  DashScopeDocumentRetriever intentKnowledgeRetriever,
                                                  DashScopeDocumentRetriever qaKnowledgeRetriever) throws GraphStateException {

        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                .addPatternStrategy(USER_QUERY, new ReplaceStrategy())
                .addPatternStrategy(HISTORY, new AppendStrategy())
                .addPatternStrategy(REWRITE_QUERY, new ReplaceStrategy())
                .addPatternStrategy(INTENT_RESULT, new ReplaceStrategy())
                .addPatternStrategy(INTENT_DESC, new ReplaceStrategy())
                .addPatternStrategy(INTENT_RAG_RESULT, new ReplaceStrategy())
                .addPatternStrategy(QA_RAG_RESULT, new ReplaceStrategy())
                .addPatternStrategy(NOW_DATE, new ReplaceStrategy())
                .addPatternStrategy(WEEK_DAY, new ReplaceStrategy())
                .addPatternStrategy(WEEK_OF_YEAR, new ReplaceStrategy())
                .addPatternStrategy(CLARIFY_LIST, new AppendStrategy())
                .addPatternStrategy(OUTPUT_SCHEMA_KEY, new ReplaceStrategy())
                .addPatternStrategy(SKIP_ASSESS_FLAG, new ReplaceStrategy())
                .addPatternStrategy(ASSESS_RESULT, new ReplaceStrategy())
                .addPatternStrategy(REPLY, new ReplaceStrategy())
                .addPatternStrategy(RESUME, new ReplaceStrategy())
                .addPatternStrategy(AGENT_TOOL_INPUT, new ReplaceStrategy())
                .addPatternStrategy(AGENT_TOOL_OUTPUT, new ReplaceStrategy())
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
        var intentRagNode = new RagNode(REWRITE_QUERY, INTENT_RAG_RESULT, intentKnowledgeRetriever);

        // 意图识别节点
        var intentNode = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .systemPrompt(resourceToString(intentPrompt))
                .userPrompt(INTENT_NODE_USER_PROMPT)
                .userParams(new HashMap<>(Map.of(USER_QUERY, "", REWRITE_QUERY, "", INTENT_RAG_RESULT, List.of())))
                .outputKey(INTENT_RESULT)
                .build();

        var intentEdge = edge_async(state -> {
            var intentResult = state.value(INTENT_RESULT, "其他场景");
            return "其他场景".equals(intentResult) ? "qa" : "analysis";
        });

        NodeAction setParamNode = (OverAllState state) -> {
            var intentResult = state.value(INTENT_RESULT, String.class).orElse("商家维度经营分析");
            var intentDesc = INTENT_DESC_MAP.get(intentResult);
            @SuppressWarnings("unchecked")
            List<RagDoc> ragDocs = (List<RagDoc>) state.value(INTENT_RAG_RESULT, List.class)
                    .orElse(Collections.emptyList());
            int totalCount = ragDocs.size();
            boolean skippAssess = false;
            if (totalCount > 0) {
                Map<String, List<RagDoc>> groupedByDocName = ragDocs.stream()
                        .collect(Collectors.groupingBy(RagDoc::docName));
                // 计算每个分组中 score > 0.6 的数量占总数的比例
                Map<String, Double> groupHighScoreRatio = groupedByDocName.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> {
                                    long highScoreCount = entry.getValue().stream()
                                            .filter(doc -> {
                                                try {
                                                    return Double.parseDouble(doc.score()) > 0.6;
                                                } catch (NumberFormatException e) {
                                                    return false;
                                                }
                                            })
                                            .count();
                                    return (double) highScoreCount / totalCount;
                                }
                        ));
                skippAssess = groupHighScoreRatio.getOrDefault(intentResult, 0.0) > 0.5;
            }
            return Map.of(INTENT_DESC, intentDesc, SKIP_ASSESS_FLAG, skippAssess ? "skip_assess" : "assess");
        };

        // 意图评估
        // 使用其他模型判断用户输入是否属于这个意图
        // 如果置信度比较低，需要用户确认
        var assessSchema = new BeanOutputConverter<>(AssessResult.class);
        var assessIntent = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.1)
                        .build())
                .systemPrompt(resourceToString(assessPrompt))
                .sysParams(new HashMap<>(Map.of(HISTORY, "", USER_QUERY, "", INTENT_RAG_RESULT, List.of(),
                        INTENT_RESULT, "", INTENT_DESC, "")))
                .outputKey(ASSESS_RESULT)
                .converter(assessSchema)
                .build();

        var assessWaitNode = new AssessWaitNode(ASSESS_RESULT, REPLY);

        var assessEdge = edge_async(state -> {
            var assessResult = state.value(ASSESS_RESULT, AssessResult.class).orElse(AssessResult.empty());
            return "2".equals(assessResult.status()) ? "tool" : "start";
        });

        var agentToolSchema = new BeanOutputConverter<>(AgentToolResult.class);
        var agentNode = LLMNode.builder()
                .chatModel(chatModel)
                .inputKey(USER_QUERY)
                .outputKey(AGENT_TOOL_OUTPUT)
                .systemPrompt("""
                        你是 wx 小助手，使用工具帮助用户解决问题，结合工具进行中文回答
                        """)
                .converter(agentToolSchema)
                .chatOptions(getAgentToolOptions())
                .build();
        var agentToolWaitNode = new AgentToolWaitNode(AGENT_TOOL_OUTPUT, REPLY);

        var agentToolEdge = edge_async(state -> {
            var assessResult = state.value(AGENT_TOOL_OUTPUT, AgentToolResult.class).orElse(AgentToolResult.empty());
            return "2".equals(assessResult.status()) ? "next" : "back";
        });


        // 知识库问答节点
        var qaRagNode = new RagNode(USER_QUERY, QA_RAG_RESULT, qaKnowledgeRetriever);

        var qaNode = LLMNode.builder()
                .chatModel(chatModel)
                .chatOptions(DashScopeChatOptions.builder()
                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3_1.value)
                        .temperature(0.7)
                        .build())
                .systemPrompt("""
                你是一个wx智能助手，根据用户问题和知识库召回的信息进行中文回答
                
                召回信息如下
                --------------------------------
                {qa_rag_list}
                --------------------------------
                """)
                .sysParams(new HashMap<>(Map.of(QA_RAG_RESULT, List.of())))
                .inputKey(USER_QUERY)
                .outputKey(REPLY)
                .build();

        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                .addNode("_rewrite_node_", node_async(rewriteNode))
                .addNode("_intent_rag_node_", node_async(intentRagNode))
                .addNode("_intent_node_", node_async(intentNode))
                .addNode("_qa_rag_node_", node_async(qaRagNode))
                .addNode("_qa_node_", node_async(qaNode))
                .addNode("_set_param_node_", node_async(setParamNode))
                .addNode("_assess_intent_node_", node_async(assessIntent))
                .addNode("_assess_wait_node_", assessWaitNode)
                .addNode("_agent_tool_node_", node_async(agentNode))
                .addNode("_agent_tool_wait_node_", agentToolWaitNode)
                .addEdge(StateGraph.START, "_rewrite_node_")
                .addEdge("_rewrite_node_", "_intent_rag_node_")
                .addEdge("_intent_rag_node_", "_intent_node_")
                .addConditionalEdges("_intent_node_",
                        intentEdge, Map.of("qa", "_qa_rag_node_", "analysis", "_set_param_node_"))
                .addEdge("_qa_rag_node_", "_qa_node_")
                .addEdge("_qa_node_", StateGraph.END)
                .addConditionalEdges("_set_param_node_", edge_async(state -> state.value(SKIP_ASSESS_FLAG, "assess")),
                        Map.of("assess", "_assess_intent_node_", "skip_assess", "_agent_tool_node_"))
                .addEdge("_assess_intent_node_", "_assess_wait_node_")
                .addConditionalEdges("_assess_wait_node_", assessEdge, Map.of("start", "_rewrite_node_", "tool", "_agent_tool_node_"))
                .addEdge("_agent_tool_node_", "_agent_tool_wait_node_")
                .addConditionalEdges("_agent_tool_wait_node_", agentToolEdge, Map.of("next", StateGraph.END, "back", "_agent_tool_node_"));
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.MERMAID, "Intent Clarify Graph");
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

    private DashScopeChatOptions getAgentToolOptions() {
        List<ToolCallback> callbacks = new ArrayList<>();
        Method method = ReflectionUtils.findMethod(toolService.getClass(), "allAnalyse", MerchantOrderIncomeTimeRequest.class);
        ToolCallback toolCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .description("商家经营数据分析功能。查询并分析指定周期内的商家经营数据，包括收入、订单量和用户数据。")
                        .name("allAnalyse")
                        .inputSchema(JsonSchemaGenerator.generateForMethodInput(method))
                        .build())
                .toolMethod(method)
                .toolObject(toolService)
                .build();

        Method method1 = ReflectionUtils.findMethod(toolService.getClass(), "downloadTool", DownloadMerchantIncomeRequest.class);
        ToolCallback toolCallback1 = MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .description("下载商家经营数据功能：按照用户的查询时间查询经营数据，并发送到指定的邮箱")
                        .name("downloadTool")
                        .inputSchema(JsonSchemaGenerator.generateForMethodInput(method1))
                        .build())
                .toolMethod(method1)
                .toolObject(toolService)
                .build();
        callbacks.add(toolCallback);
        callbacks.add(toolCallback1);
        return DashScopeChatOptions.builder()
                .toolCallbacks(callbacks)
                .model(DashScopeModel.ChatModel.QWEN3_MAX.value)
                .temperature(0.7)
                .build();
    }

}
