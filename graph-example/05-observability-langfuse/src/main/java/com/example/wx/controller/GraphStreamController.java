package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.wx.controller.process.GraphProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/graph/observation")
public class GraphStreamController {
    private static final Logger logger = LoggerFactory.getLogger(GraphStreamController.class);

    @Autowired
    private CompiledGraph compiledGraph;

    /**
     * Stream graph processing execution
     * @param input the input content to process
     * @param threadId the thread ID for processing isolation
     * @return SSE streaming output
     * @throws GraphRunnerException if graph execution fails
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(
            @RequestParam(value = "prompt", defaultValue = "请分析这段文本：人工智能的发展") String input,
            @RequestParam(value = "thread_id", defaultValue = "observability", required = false) String threadId)
            throws GraphRunnerException {

        logger.info("Starting streaming graph execution, input: {}, thread ID: {}", input, threadId);

        // Create runnable configuration
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();

        // Create initial state
        Map<String, Object> initialState = new HashMap<>();
        initialState.put("input", input);

        // Create graph processor
        GraphProcess graphProcess = new GraphProcess();

        // Get streaming output
        Flux<NodeOutput> resultStream = compiledGraph.fluxStream(initialState, runnableConfig);


        // 直接返回 Reactor 风格的 Flux，保证 trace context 传播
        return graphProcess.processStream(resultStream)
                .doOnCancel(() -> logger.info("Client disconnected from streaming"))
                .doOnError(e -> logger.error("Error occurred during streaming output", e))
                .doOnComplete(() -> logger.info("Streaming output completed"));
    }
}