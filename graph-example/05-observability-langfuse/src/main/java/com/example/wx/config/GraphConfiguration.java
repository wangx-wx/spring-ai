package com.example.wx.config;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.constant.SaverEnum;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.node.ChatNode;
import com.example.wx.node.MergeNode;
import com.example.wx.node.SimpleSubGraph;
import com.example.wx.node.StreamingChatNode;
import com.google.common.collect.Lists;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author wangxiang
 * @description
 * @create 2025/10/25 19:31
 */
@Configuration
public class GraphConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(GraphConfiguration.class);


    /**
     * Configure ChatClient with logging advisor
     *
     * @param chatModel the chat model to use
     * @return configured ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        // 添加 AI 模型可用性测试
        try {
            logger.info("Testing AI model availability...");
            logger.info("ChatModel type: {}", chatModel.getClass().getSimpleName());
            logger.info("ChatModel default options: {}", chatModel.getDefaultOptions());

            // 尝试一个简单的同步调用测试
            ChatClient testClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
            String testResponse = testClient.prompt().user("Hello").call().content();
            logger.info("AI model test successful, response: {}",
                    testResponse.substring(0, Math.min(50, testResponse.length())));
        } catch (Exception e) {
            logger.error("AI model test failed: {}", e.getMessage(), e);
        }
        return ChatClient.builder(chatModel).defaultSystem("使用中文回答").defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }

    @Bean
    public RestClient.Builder createRestClient() {

        // 2. 创建 RequestConfig 并设置超时
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(10, TimeUnit.MINUTES)) // 设置连接超时
                .setResponseTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .setConnectionRequestTimeout(Timeout.of(10, TimeUnit.MINUTES))
                .build();

        // 3. 创建 CloseableHttpClient 并应用配置
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        // 4. 使用 HttpComponentsClientHttpRequestFactory 包装 HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 5. 创建 RestClient 并设置请求工厂
        return RestClient.builder().requestFactory(requestFactory);
    }

    /**
     * Configure the observability graph
     *
     * @param chatClient the chat client for AI processing
     * @return configured StateGraph
     * @throws GraphStateException if graph configuration fails
     */
    @Bean
    public StateGraph observabilityGraph(ChatClient chatClient) throws GraphStateException {
        // start node - initial processing
        ChatNode starNode = ChatNode.create("StartNode", "input", "start_output", chatClient,
                "Please perform initial processing on the input context:");

        // Parallel nodes - concurrent processing
        ChatNode parallelNode1 = ChatNode.create("ParallelNode1", "start_output", "parallel_output1", chatClient,
                "Please perform sentiment analysis on the content:");
        ChatNode parallelNode2 = ChatNode.create("ParallelNode2", "start_output", "parallel_output2", chatClient,
                "Please perform topic analysis on the content:");

        // Summary node - aggregates streaming output
        ChatNode summaryNode = ChatNode.create("SummaryNode", "streaming_output", "summary_output", chatClient,
                "Please summarize the streaming analysis results:");

        // Merge node - combine parallel outputs for subgraph input
        MergeNode mergeNode = new MergeNode(Lists.newArrayList("parallel_output1", "parallel_output2"), "sub_input");


        // Streaming node - real-time AI response
        StreamingChatNode streamingNode = StreamingChatNode.create("StreamingNode", "subgraph_final_output", "streaming_output", chatClient,
                "Please perform detailed analysis on the subgraph results:");

        // End node -final output formatting
        ChatNode endNode = ChatNode.create("EndNode", "summary_output", "end_output", chatClient,
                "Please format teh final results for output:");

        SimpleSubGraph subGraph = new SimpleSubGraph(chatClient);

        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                .addPatternStrategy("input", new ReplaceStrategy())
                .addPatternStrategy("start_output", new ReplaceStrategy())
                .addPatternStrategy("parallel_output1", new ReplaceStrategy())
                .addPatternStrategy("parallel_output2", new ReplaceStrategy())
                .addPatternStrategy("sub_input", new ReplaceStrategy())
                .addPatternStrategy("sub_output1", new ReplaceStrategy())
                .addPatternStrategy("sub_output2", new ReplaceStrategy())
                .addPatternStrategy("_subgraph", new ReplaceStrategy())
                // .addPatternStrategy("final_output", new ReplaceStrategy())
                .addPatternStrategy("subgraph_final_output", new ReplaceStrategy())
                .addPatternStrategy("streaming_output", new ReplaceStrategy())
                .addPatternStrategy("summary_output", new ReplaceStrategy())
                .addPatternStrategy("end_output", new ReplaceStrategy())
                .addPatternStrategy("logs", new AppendStrategy())
                .build();

        StateGraph stateGraph = new StateGraph(keyStrategyFactory);
        stateGraph.addNode("start", node_async(starNode))
                .addNode("parallel1", node_async(parallelNode1))
                .addNode("parallel2", node_async(parallelNode2))
                .addNode("merge", node_async(mergeNode))
                .addNode("subgraph", subGraph.subGraph())
                .addNode("streaming", node_async(streamingNode))
                .addNode("summary", node_async(summaryNode))
                .addNode("end", node_async(endNode))

                .addEdge(START, "start")
                .addEdge("start", "parallel1")
                .addEdge("start", "parallel2")
                .addEdge("parallel1", "merge")
                .addEdge("parallel2", "merge")
                .addEdge("merge", "subgraph")
                .addEdge("subgraph", "streaming")
                .addEdge("streaming", "summary")
                .addEdge("summary", "end")
                .addEdge("end", END);
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "Observability Demo");

        System.out.println(" \n === Observability Demo Graph === ");
        System.out.println(representation.content());
        System.out.println("=====================\n");
        return stateGraph;
    }

    @Bean
    public CompiledGraph compiledGraph(StateGraph observabilityGraph, CompileConfig observationCompileConfig)
            throws GraphStateException {
        // 为子图添加 checkpoint saver 配置，确保子图能正确接收输入
        CompileConfig subgraphCompileConfig = CompileConfig.builder(observationCompileConfig)
                .saverConfig(SaverConfig.builder().register(SaverEnum.MEMORY.getValue(), new MemorySaver()).build())
                .build();

        return observabilityGraph.compile(subgraphCompileConfig);
    }
}
