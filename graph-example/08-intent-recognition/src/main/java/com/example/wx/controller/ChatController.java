package com.example.wx.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import com.example.wx.config.GraphListener;
import com.example.wx.domain.ChatResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.example.wx.constants.IntentGraphParams.NOW_DATE;
import static com.example.wx.constants.IntentGraphParams.REPLY;
import static com.example.wx.constants.IntentGraphParams.RESUME;
import static com.example.wx.constants.IntentGraphParams.USER_QUERY;
import static com.example.wx.constants.IntentGraphParams.WEEK_DAY;
import static com.example.wx.constants.IntentGraphParams.WEEK_OF_YEAR;

/**
 * @author wangx
 * @description
 * @create 2025/12/23 22:06
 */
@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {
    private final CompiledGraph compiledGraph;

    public ChatController(StateGraph issueClarifyGraph
            , GraphListener graphListener
//            , MemorySaver postgresSaver
    ) throws GraphStateException {
        var saver = new MemorySaver();
        var compileConfig = CompileConfig.builder()
                // .withLifecycleListener(graphListener)
                .saverConfig(SaverConfig.builder()
                        .register(saver)
                        .build())
                .build();
        this.compiledGraph = issueClarifyGraph.compile(compileConfig);
    }

    @GetMapping("/call")
    public Mono<String> call(
            @RequestParam(value = "query", defaultValue = "你好，你可以做什么") String query,
            @RequestParam(value = "chatId", defaultValue = "chat-id-10001") String chatId) {
        // 构建运行时配置，threadId 用于标识会话
        var config = RunnableConfig.builder()
                .threadId(chatId)
                .build();

        // 执行工作流并返回最终的 reply 字段
        return getExecutionStream(query, config)
                .last()
                .map(output ->
                        output.state().value(REPLY, String.class).orElse("完成")
                );
    }

    /**
     * SSE流式接口，实时推送节点执行结果
     *
     * @param query  用户输入
     * @param chatId 会话ID
     * @return Flux<ChatResult> 流式聊天结果
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResult> stream(
            @RequestParam(value = "query", defaultValue = "你好，你可以做什么") String query,
            @RequestParam(value = "chatId", defaultValue = "chat-id-10001") String chatId) {
        var config = RunnableConfig.builder()
                .threadId(chatId)
                .build();

        return getExecutionStream(query, config)
                .doOnNext(nodeOutput -> {
                    System.out.println("===== 收到 NodeOutput，开始转换为 ChatResult =====");
                })
                .map(this::toChatResult)
                .doOnNext(chatResult -> {
                    System.out.println("===== ChatResult 准备发送: " + chatResult.getNodeName() + " =====");
                })
                .concatWithValues(ChatResult.end());
    }

    /**
     * 将 NodeOutput 转换为 ChatResult
     */
    private ChatResult toChatResult(NodeOutput output) {
        String nodeName = output.node();
        String content = output.state().value(REPLY, String.class).orElse("");

        // 根据节点名称判断类型
        String type = switch (nodeName) {
            case "error_node", "end_node" -> "end";
            default -> "text";
        };

        return ChatResult.builder()
                .type(type)
                .nodeName(nodeName)
                .content(content)
                .build();
    }

    @GetMapping(value = "/stream2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResult> stream2(
            @RequestParam(value = "query", defaultValue = "你好，你可以做什么") String query,
            @RequestParam(value = "chatId", defaultValue = "chat-id-10001") String chatId) {
        var config = RunnableConfig.builder()
                .threadId(chatId)
                .build();
        // 创建 Sink，unicast 适用于单订阅者场景
        // Sinks.Many<ChatResult> sink = Sinks.many().unicast().onBackpressureBuffer();
        // getExecutionStream(query, config).doOnNext(nodeOutput -> {
        //             // 手动发送数据
        //             ChatResult result = toChatResult(nodeOutput);
        //             sink.tryEmitNext(result);
        //         })
        //         .doOnComplete(() -> {
        //             // 发送结束标记并关闭
        //             sink.tryEmitNext(ChatResult.end());
        //             sink.tryEmitComplete();
        //         })
        //         .doOnError(sink::tryEmitError)
        //         .subscribe();  // 启动订阅
        // return sink.asFlux();
        return Flux.create(emitter -> {
            getExecutionStream(query, config)
                    .doOnNext(nodeOutput -> emitter.next(toChatResult(nodeOutput)))
                    .doOnComplete(() -> {
                        emitter.next(ChatResult.end());
                        emitter.complete();
                    })
                    .doOnError(emitter::error)
                    .subscribe();
        });
    }

    private Flux<NodeOutput> getExecutionStream(String query,
                                                RunnableConfig config) {
        // 尝试获取历史状态快照
        return compiledGraph.stateOf(config)
                // 有历史状态：从检查点恢复
                .map(snapshot -> resumeFromCheckpoint(snapshot, query, config))
                // 无历史状态：开始新会话
                .orElseGet(() -> startNewConversation(query, config))
                .doOnNext(event -> log.info("节点输出：{}", event))
                .doOnError(error -> log.error("流错误：{}", error.getMessage()))
                .doOnComplete(() -> log.info("流完成"));
    }

    private Flux<NodeOutput> resumeFromCheckpoint(StateSnapshot snapshot, String query, RunnableConfig config) {
        String nextNode = snapshot.next();
        // 如果流程已完成（无下一节点或到达 END），则当作新对话处理
        if (nextNode == null || nextNode.isEmpty() || END.equals(nextNode)) {
            return startNewConversation(query, config);
        }
        try {
            // 设置恢复标记，ClarifyWaitNode 会检查此标记来判断是否应该中断
            config.context().put(RESUME, Boolean.TRUE);
            // 更新状态：将用户新输入写入 user_query
            RunnableConfig runnableConfig = this.compiledGraph.updateState(config, Map.of(
                    USER_QUERY, query
            ), null);
            // 从中断点继续执行工作流
            return this.compiledGraph.stream(null, runnableConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Flux<NodeOutput> startNewConversation(String query, RunnableConfig config) {
        // 初始化所有状态键
        Map<String, Object> initialState = Map.of(
                USER_QUERY, query,
                REPLY, (String) "",
                NOW_DATE, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                WEEK_DAY, DateUtil.dayOfWeek(DateUtil.date()),
                WEEK_OF_YEAR, DateUtil.dayOfWeekEnum(DateUtil.date()).toChinese()
        );
        // 从初始状态开始执行工作流
        return this.compiledGraph.stream(initialState, config);
    }
}
