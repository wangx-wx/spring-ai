package com.example.wx.node;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.FluxConverter;
import com.alibaba.cloud.ai.graph.streaming.StreamingChatGenerator;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/10/25 17:44
 */
public class StreamingChatNode implements NodeAction {
    private static final Logger logger = LoggerFactory.getLogger(StreamingChatNode.class);

    private final String nodeName;

    private final String inputKey;

    private final String outputKey;

    private final ChatClient chatClient;

    private final String prompt;

    /**
     * Constructor for StreamingChatNode
     *
     * @param nodeName   the name of the node
     * @param inputKey   the key for input data
     * @param outputKey  the key for output data
     * @param chatClient the chat client for AI processing
     * @param prompt     the prompt template
     */
    public StreamingChatNode(String nodeName, String inputKey, String outputKey, ChatClient chatClient, String prompt) {
        this.nodeName = nodeName;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.chatClient = chatClient;
        this.prompt = prompt;
    }

    /**
     * Factory method to create StreamingChatNode
     *
     * @param nodeName   the name of the node
     * @param inputKey   the key for input data
     * @param outputKey  the key for output data
     * @param chatClient the chat client for AI processing
     * @param prompt     the prompt template
     * @return StreamingChatNode instance
     */
    public static StreamingChatNode create(String nodeName, String inputKey, String outputKey, ChatClient chatClient,
                                           String prompt) {
        return new StreamingChatNode(nodeName, inputKey, outputKey, chatClient, prompt);
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        logger.info("{} starting streaming processing", nodeName);
        String inputData = state.value(inputKey).map(Object::toString).orElse("Default input");

        logger.info("{} input data: {}", nodeName, inputData);

        // Build complete prompt
        String fullPrompt = prompt + " Input content: " + inputData;

        // 添加调试信息
        logger.info("{} full prompt length: {} characters", nodeName, fullPrompt.length());
        logger.info("{} using ChatClient: {}", nodeName, chatClient.getClass().getSimpleName());

        try {
            Flux<ChatResponse> chatResponseFlux = chatClient.prompt()
                    .user(fullPrompt)
                    .stream()
                    .chatResponse()
                    .doOnSubscribe(sub -> logger.info("{} chatResponseFlux subscribed", nodeName))
                    .doOnNext(resp -> logger.info("{} chatResponseFlux emit: {}", nodeName, resp))
                    .doOnError(e -> logger.error("{} chatResponseFlux error", nodeName, e))
                    .doOnComplete(() -> logger.info("{} chatResponseFlux completed", nodeName))
                    .timeout(Duration.ofMinutes(3))
                    .onErrorResume(e -> {
                        logger.error("{} chatResponseFlux timeout or error, using fallback", nodeName, e);
                        return Flux.empty();
                    });

            Flux<GraphResponse<StreamingOutput>> generator = FluxConverter.builder()
                    .startingNode(nodeName + "_stream")
                    .startingState(state)
                    .mapResult(resp -> {
                        String content = resp.getResult().getOutput().getText();
                        logger.info("{} mapResult emit content: {}", nodeName, content);
                        return Map.of(outputKey, content);
                    })
                    .build(chatResponseFlux);

            // AsyncGenerator<? extends NodeOutput> generator = StreamingChatGenerator.builder()
            //         .startingNode(nodeName + "_stream")
            //         .startingState(state)
            //         .mapResult(response -> {
            //             String content = response.getResult().getOutput().getText();
            //             logger.info("{}: mapResult emit chunk: {}", nodeName, content);
            //             return Map.of(outputKey, content);
            //         })
            //         .build(chatResponseFlux);


            logger.info("{} streaming processing setup completed", nodeName);
            return Map.of(outputKey, generator);
        } catch (Exception e) {
            logger.error("{} streaming processing failed: {}", nodeName, e.getMessage(), e);
            String fallbackResult = String.format("[%s] streaming failed, fallback processing: %s ", nodeName, inputData);
            return Map.of(outputKey, fallbackResult);
        }
    }
}
