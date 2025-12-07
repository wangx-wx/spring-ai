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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/7 22:07
 */
@RestController
public class TestController {

    private final CompiledGraph compiledGraph;

    public TestController(@Qualifier("slotAnalysisGraph") StateGraph slotAnalysisGraph) throws GraphStateException {
        var saver = new MemorySaver();
        var compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(saver)
                        .build())
                .build();
        this.compiledGraph = slotAnalysisGraph.compile(compileConfig);
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
