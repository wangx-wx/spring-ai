package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/10/7 10:29
 */
@RestController
@RequestMapping("/analyze")
public class ParallelController {
    private final CompiledGraph engine;

    public ParallelController(@Qualifier("parallelGraph") StateGraph parallelGraph) throws GraphStateException {
        SaverConfig saverConfig = SaverConfig.builder().build();
        // 编译时可设中断点
        this.engine = parallelGraph
                .compile(CompileConfig.builder().saverConfig(saverConfig).interruptBefore("merge").build());
    }

    /**
     * 调用写作助手流程图 示例请求：GET /analyze?text=今天我去了西湖，天气特别好，感觉特别开心
     */
    @GetMapping
    public Map<String, Object> analyze(@RequestParam("text") String text) {
        return engine.call(Map.of("inputText", text)).get().data();
    }

    @GetMapping(path = "/stream", produces = "text/event-stream")
    public Flux<Map<String, Object>> analyzeStream(@RequestParam("text") String text) {
        RunnableConfig cfg = RunnableConfig.builder().streamMode(CompiledGraph.StreamMode.SNAPSHOTS).build();
        return Flux.create(sink -> {
            engine.fluxStream(Map.of("inputText", text), cfg).doOnNext(
                    node -> sink.next(node.state().data())
            ).doOnComplete(sink::complete).doOnError(sink::error).subscribe();
        });
    }
}
