package com.example.wx.node;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.streaming.FluxConverter;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/27 22:01
 */
public class ExpanderNode implements NodeAction {

    private static final Logger logger = LoggerFactory.getLogger(ExpanderNode.class);

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
            You are an expert at information retrieval and search optimization.
            Your task is to generate {number} different versions of the given query.
            Each variant must cover different perspectives or aspects of the topic,
            while maintaining the core intent of the original query. The goal is to
            expand the search space and improve the chances of finding relevant information.
            Do not explain your choices or add any other text.
            Provide the query variants separated by newlines.
            Original query: {query}
            Query variants:
            
            """);

    private final ChatClient chatClient;

    private final static Integer NUMBER = 3;

    public ExpanderNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        logger.info("expander node is running.");
        String query = state.value("query", "");
        Integer expanderNumber = state.value("expander_number", NUMBER);

        Flux<ChatResponse> chatResponseFlux = this.chatClient.prompt()
                .user((user) -> user.text(DEFAULT_PROMPT_TEMPLATE.getTemplate())
                        .param("number", expanderNumber)
                        .param("query", query))
                .stream().chatResponse();

        Flux<GraphResponse<StreamingOutput>> nodeOutputs = FluxConverter.builder()
                .startingNode("expander_llm_stream")
                .startingState(state)
                .mapResult(response -> {
                    String text = response.getResult().getOutput().getText();
                    List<String> queryVariants = Arrays.asList(text.split("\n"));
                    return Map.of("expander_content", queryVariants);
                })
                .build(chatResponseFlux);
        return Map.of("expander_content", nodeOutputs);
    }
}
