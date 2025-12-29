package com.example.wx.config;

import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.KeyStrategyFactoryBuilder;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.AgentStateFactory;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.example.wx.model.Product;
import com.example.wx.serializer.ProductStateSerializer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author wangx
 * @description
 * @create 2025/10/8 18:09
 */
@Configuration
public class ProductGraphConfig {
    @Bean
    public StateGraph productAnalysisGraph(ChatClient.Builder chatClientBuilder) throws GraphStateException {
        ChatClient client = chatClientBuilder.build();
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactoryBuilder()
                .addPatternStrategy("productDesc", new ReplaceStrategy())
                .addPatternStrategy("slogan", new ReplaceStrategy())
                .addPatternStrategy("productSpec", new ReplaceStrategy())
                .addPatternStrategy("finalProduct", new ReplaceStrategy())
                .build();

        // Create custom serializer to handle Product object serialization
        AgentStateFactory<OverAllState> stateFactory = OverAllState::new;
        ProductStateSerializer serializer = new ProductStateSerializer(stateFactory);

        NodeAction marketingCopyNode = state -> {
            String productDesc = (String) state.value("productDesc").orElseThrow();
            String slogan = client.prompt()
                    .user("Generate a catchy slogan for a product with the following description: " + productDesc)
                    .call()
                    .content();
            return Map.of("slogan", slogan);
        };

        NodeAction specificationExtractionNode = (state) -> {
            String productDesc = (String) state.value("productDesc").orElseThrow();
            Product productSpec = client.prompt()
                    .user("Extract product specifications from the following description: " + productDesc)
                    .call()
                    .entity(Product.class);
            return Map.of("productSpec", productSpec);
        };

        NodeAction mergeNode = state -> {
            String slogan = state.value("slogan", String.class).orElseThrow();
            Product productSpec = state.value("productSpec", Product.class).orElseThrow();

            Product finalProduct = new Product(slogan, productSpec.material(), productSpec.colors(), productSpec.season());
            return Map.of("finalProduct", finalProduct);
        };

        StateGraph graph = new StateGraph("ProductAnalysisGraph", keyStrategyFactory, serializer);
        graph.addNode("marketingCopy", node_async(marketingCopyNode))
                .addNode("specificationExtraction", node_async(specificationExtractionNode))
                .addNode("merge", node_async(mergeNode))
                .addEdge(START, "marketingCopy")
                .addEdge(START, "specificationExtraction")
                .addEdge("marketingCopy", "merge")
                .addEdge("specificationExtraction", "merge")
                .addEdge("merge", END);
        GraphRepresentation representation = graph.getGraph(GraphRepresentation.Type.PLANTUML, "Product Analysis Graph");
        System.out.println("\n=== Product Analysis Graph UML Flow ===");
        System.out.println(representation.content());
        System.out.println("======================================\n");

        return graph;
    }
}
