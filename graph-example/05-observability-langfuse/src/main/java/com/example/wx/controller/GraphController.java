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

        return Mono.fromCallable(() -> {
                    // 1️⃣ 创建初始状态
                    Map<String, Object> initialState = Map.of("input", input);
                    RunnableConfig runnableConfig = RunnableConfig.builder().build();

                    // 2️⃣ 调用图执行（内部仍然是阻塞式 get）
                    OverAllState result = compiledGraph.call(initialState, runnableConfig).get();

                    // 3️⃣ 封装返回结果
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("input", input);
                    response.put("output", result.value("end_output").orElse("No output"));
                    response.put("logs", result.value("logs").orElse("No logs"));

                    logger.info("分析成功：{}", response);
                    return response;
                })
                // ⚠️ 关键：切换到阻塞专用线程池执行
                .subscribeOn(Schedulers.boundedElastic())
                // 处理异常
                .onErrorResume(e -> {
                    logger.error("异常结束 [{}]", e.getMessage(), e);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(errorResponse);
                });
    }

}
