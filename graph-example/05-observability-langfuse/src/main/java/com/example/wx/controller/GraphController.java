package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/10/25 23:49
 */
@RestController
@RequestMapping("/graph/observation")
public class GraphController {

    private final static Logger logger = LoggerFactory.getLogger(GraphController.class);

    @Autowired
    private CompiledGraph compiledGraph;

    @GetMapping("/execute")
    public Mono<Map<String, Object>> execute(
            @RequestParam(value = "prompt", defaultValue = "请分析这段文本：人工智能的发展") String input) {

        // 1️⃣ 创建初始状态
        Map<String, Object> initialState = Map.of("input", input);
        RunnableConfig runnableConfig = RunnableConfig.builder().build();

        return compiledGraph.stream(initialState, runnableConfig)
                .doOnNext(event -> logger.debug("Graph node output: {}", event))
                .doOnError(e -> logger.error("Graph stream error", e))
                .doOnComplete(() -> logger.info("Graph stream completed"))
                .last()
                .map(output->{
                    // 3️⃣ 封装返回结果
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("input", input);
                    response.put("output", output.state().value("end_output").orElse("No output"));
                    response.put("logs", output.state().value("logs").orElse("No logs"));

                    logger.info("分析成功：{}", response);
                    return response;
                });
    }

}
