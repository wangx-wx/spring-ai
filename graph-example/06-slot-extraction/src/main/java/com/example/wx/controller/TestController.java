package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import com.example.wx.dto.GraphRequest;
import com.example.wx.dto.Result;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wangx
 * @description
 * @create 2025/12/7 22:07
 */
@RestController
public class TestController {

    private final CompiledGraph compiledGraph;
    AtomicReference<NodeOutput> lastOutputRef = new AtomicReference<>();

    public TestController(@Qualifier("slotAnalysisGraph") StateGraph slotAnalysisGraph) throws GraphStateException {
        var saver = new MemorySaver();
        var compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(saver)
                        .build())
                .build();
        this.compiledGraph = slotAnalysisGraph.compile(compileConfig);
    }

    @GetMapping("/call")
    public Mono<String> call(GraphRequest request) {
        var config = RunnableConfig.builder()
                .threadId(request.threadId())
                .build();

        return getExecutionStream(request, config)
                .last()
                .map(output -> {
                    // 检查是否是中断
                    if (output instanceof InterruptionMetadata interruption) {
                        // 中断场景：从元数据获取 reply
                        return (String) interruption.metadata("reply").orElse("请补充信息");
                    }

                    // 正常完成：从状态获取 reply
                    return output.state().value("reply", String.class).orElse("完成");
                });
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(GraphRequest request) {
        var config = RunnableConfig.builder()
                .threadId(request.threadId())
                .build();

        return getExecutionStream(request, config)
                .last()
                .map(output -> output.state().value("slot_params", Result.class).get().toString())
                .flux(); // 转换为 Flux
    }

    private Flux<NodeOutput> startNewConversation(GraphRequest request, RunnableConfig config) {
        Map<String, Object> initialState = Map.of(
                "user_query", request.query()
        );
        return this.compiledGraph.stream(initialState, config).doOnNext(event -> {
                    System.out.println("节点输出: " + event);
                    lastOutputRef.set(event);
                })
                .doOnError(error -> System.err.println("流错误: " + error.getMessage()))
                .doOnComplete(() -> System.out.println("流完成"));
    }

    private Flux<NodeOutput> getExecutionStream(GraphRequest request,
                                                RunnableConfig config) {
        return compiledGraph.stateOf(config)
                .map(snapshot -> resumeFromCheckpoint(snapshot, request, config))
                .orElseGet(() -> startNewConversation(request, config));
    }

    private Flux<NodeOutput> resumeFromCheckpoint(StateSnapshot snapshot, GraphRequest request, RunnableConfig config) {
        String nextNode = snapshot.next();
        if (nextNode == null || nextNode.isEmpty()) {
            // 流程已完成,当新对话处理
            return startNewConversation(request, config);
        }
        try {
            RunnableConfig runnableConfig = this.compiledGraph.updateState(config, Map.of(
                    "user_query", request.query()
            ), null);
            return this.compiledGraph.stream(null, runnableConfig)
                    .doOnNext(event -> {
                        System.out.println("节点输出: " + event);
                        lastOutputRef.set(event);
                    })
                    .doOnError(error -> System.err.println("流错误: " + error.getMessage()))
                    .doOnComplete(() -> System.out.println("流完成"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/test")
    public void test(@RequestParam("user") String user) {
        // 初始输入
        Map<String, Object> initialInput = Map.of("user_query", user, "nowDate", "2025-12-07");

        // 配置线程 ID
        var invokeConfig = RunnableConfig.builder()
                .threadId("Thread1")
                .build();

        // 用于保存最后一个输出
        AtomicReference<NodeOutput> lastOutputRef = new AtomicReference<>();

        // 运行 Graph 直到第一个中断点
        compiledGraph.stream(initialInput, invokeConfig)
                .doOnNext(event -> {
                    System.out.println("节点输出: " + event);
                    lastOutputRef.set(event);
                })
                .doOnError(error -> System.err.println("流错误: " + error.getMessage()))
                .doOnComplete(() -> System.out.println("流完成"))
                .blockLast();

        // 检查最后一个输出是否是 InterruptionMetadata
        NodeOutput lastOutput = lastOutputRef.get();
        if (lastOutput instanceof InterruptionMetadata) {
            System.out.println("检测到中断: " + lastOutput);
        }
    }

}
