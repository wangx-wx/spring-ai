package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.example.wx.domain.ChatResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2026/1/11 15:34
 */
@RestController
@RequestMapping("/ai/stream")
public class StreamController {

    private final CompiledGraph compiledGraph;

    public StreamController(StateGraph streamGraph) throws GraphStateException {
        var saver = new MemorySaver();
        var compileConfig = CompileConfig.builder()
                .saverConfig(SaverConfig.builder()
                        .register(saver)
                        .build())
                .build();
        this.compiledGraph = streamGraph.compile(compileConfig);
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResult> stream() {
        var config = RunnableConfig.builder()
                .threadId("asd")
                .build();
        return compiledGraph.stream(Map.of("query", "请用一句话介绍 Spring AI"), config)
                .doOnNext(output -> {
                    // 处理流式输出
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        // 流式输出块
                        String chunk = streamingOutput.chunk();
                        if (chunk != null && !chunk.isEmpty()) {
                            System.out.print(chunk); // 实时打印流式内容
                        }
                    } else {
                        // 普通节点输出
                        String nodeId = output.node();
                        Map<String, Object> state = output.state().data();
                        System.out.println(" 节点 '" + nodeId + "' 执行完成");
                        if (state.containsKey("result")) {
                            System.out.println("最终结果: " + state.get("result"));
                        }
                    }
                })
                .doOnComplete(() -> {
                    System.out.println("流式输出完成");
                })
                .doOnError(error -> {
                    System.err.println("流式输出错误: " + error.getMessage());
                })
                .map(this::toChatResult);
    }
    private ChatResult toChatResult(NodeOutput output) {
        String nodeName = output.node();
        String content = output.state().toString();

        // 根据节点名称判断类型
        String type = switch (nodeName) {
            case "error_node", "end_node" -> "end";
            default -> "text";
        };

        return ChatResult.builder()
                .nodeName(nodeName)
                .content(content)
                .build();
    }

}
