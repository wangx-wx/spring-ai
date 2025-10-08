package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.constant.SaverEnum;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;

/**
 * @author wangxiang
 * @description
 * @create 2025/9/28 22:15
 */
@RestController
@RequestMapping("/graph/human")
public class GraphHumanController {

    private static final Logger logger = LoggerFactory.getLogger(GraphHumanController.class);

    private final CompiledGraph compiledGraph;

    public GraphHumanController(@Qualifier("humanGraph") StateGraph stateGraph) throws GraphStateException {
        SaverConfig saverConfig = SaverConfig.builder()
                .register(SaverEnum.MEMORY.getValue(), new MemorySaver())
                .build();

        this.compiledGraph = stateGraph
                .compile(
                        CompileConfig.builder()
                                .saverConfig(saverConfig)
                                .interruptBefore("human_feedback")
                                .build()
                );
    }

    @GetMapping(value = "/expend", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> expand(
            @RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？", required = false) String query,
            @RequestParam(value = "expander_number", defaultValue = "3", required = false) Integer expenderNumber,
            @RequestParam(value = "thread_id", defaultValue = "wx", required = false) String threadId
    ) throws GraphRunnerException {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(threadId).build();
        HashMap<String, Object> objectMap = new HashMap<>();
        objectMap.put("query", query);
        objectMap.put("expander_number", expenderNumber);

        GraphProcess graphProcess = new GraphProcess(this.compiledGraph);
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        AsyncGenerator<NodeOutput> resultFuture = compiledGraph.stream(objectMap, runnableConfig);

        graphProcess.processStream(resultFuture, sink);
        return sink.asFlux()
                .doOnCancel(() -> logger.info("client disconnected from stream"))
                .doOnError(e -> logger.error("Error occurred during streaming", e));
    }
}
