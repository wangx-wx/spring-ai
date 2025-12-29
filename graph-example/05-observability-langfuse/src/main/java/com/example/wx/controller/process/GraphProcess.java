package com.example.wx.controller.process;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/10/25 23:48
 */
public class GraphProcess {

    private static final Logger logger = LoggerFactory.getLogger(GraphProcess.class);

    public GraphProcess() {
    }


    public Flux<ServerSentEvent<String>> processStream(Flux<NodeOutput> generator) {
        return Flux.create(sink -> processNext(generator, sink));
    }

    public void processNext(Flux<NodeOutput> generator,
                            reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink) {
        generator.subscribe(
                output -> {
                    logger.info("processNext: output node={}, output class={}, output={}",
                            output != null ? output.node() : null,
                            output != null ? output.getClass().getName() : null,
                            output
                    );

                    String content;
                    if (output instanceof StreamingOutput streamingOutput) {
                        content = JSON.toJSONString(Map.of("type", "streaming", "node", output.node(), "chunk", streamingOutput.chunk(),
                                "timestamp", System.currentTimeMillis()));
                    } else {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("type", "node_output");
                        jsonObject.put("node", output.node());
                        jsonObject.put("data", output.state().data());
                        jsonObject.put("timestamp", System.currentTimeMillis());
                        content = JSON.toJSONString(jsonObject);
                    }
                    logger.debug("processNext: emitting SSE event for node {}",
                            output != null ? output.node() : null);

                    sink.next(ServerSentEvent.builder(content)
                            .event("node_output")
                            .id(output.node() + "_" + System.currentTimeMillis())
                            .build());
                },
                error -> {
                    logger.error("processNext: Error occurred in data stream", error);
                    sink.next(ServerSentEvent.builder("{\"type\":\"error\",\"message\":\"" + error.getMessage() + "\"}")
                            .event("error")
                            .build());
                    sink.next(ServerSentEvent.builder("{\"type\":\"completed\",\"message\":\"Graph processing completed with error\"}")
                            .event("completed")
                            .build());
                    sink.complete();
                },
                () -> {
                    logger.debug("processNext: Graph processing completed");
                    sink.next(ServerSentEvent.builder("{\"type\":\"completed\",\"message\":\"Graph processing completed\"}")
                            .event("completed")
                            .build());
                    sink.complete();
                }
        );
    }

}
