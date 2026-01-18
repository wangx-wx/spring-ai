package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.agent.Agent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/")
public class A2AClientController {


    private final Agent rootAgent;

    public A2AClientController(Agent rootAgent) {
        this.rootAgent = rootAgent;
    }

    /**
     * 非流式调用
     * @param question
     * @return
     * @throws GraphRunnerException
     */ 
    @GetMapping("sync")
    public Object sync(@RequestParam("question") String question) throws GraphRunnerException {
        System.out.println(question);
        return rootAgent.invoke(question)
            .orElseThrow()
            .value("messages")
            .orElseThrow();
    }


    /**
     * 流式调用
     * @param question
     * @return
     * @throws GraphRunnerException
     */
    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam("question") String question) throws GraphRunnerException {
        System.out.println(question);
        return rootAgent.stream(question)
            .filter(nodeOutput -> !nodeOutput.isSTART() && !nodeOutput.isEND()) // 过滤掉开始和结束事件
            .map(nodeOutput -> {
                if (nodeOutput instanceof StreamingOutput) {
                    return ((StreamingOutput) nodeOutput).chunk();
                } else {
                    Map<String, Object> data = nodeOutput.state().data();
                    if (data.containsKey("messages")) {
                        return String.valueOf(data.get("messages"));
                    }
                    return nodeOutput.toString();
                }
            })
            .filter(StringUtils::hasText);
    }
}