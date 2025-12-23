package com.example.wx.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Function;

public class TraceLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger log = LoggerFactory.getLogger(TraceLoggerAdvisor.class);

    public static final Function<ChatClientRequest, String> DEFAULT_REQUEST_TO_STRING = Record::toString;

    public static final Function<ChatResponse, String> DEFAULT_RESPONSE_TO_STRING = ModelOptionsUtils::toJsonString;

    private final Function<ChatClientRequest, String> requestToString;

    private final Function<ChatResponse, String> responseToString;

    private final int order;

    public TraceLoggerAdvisor() {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, 0);
    }

    public TraceLoggerAdvisor(int order) {
        this(DEFAULT_REQUEST_TO_STRING, DEFAULT_RESPONSE_TO_STRING, order);
    }

    public TraceLoggerAdvisor(Function<ChatClientRequest, String> requestToString,
            Function<ChatResponse, String> responseToString, int order) {
        this.requestToString = requestToString;
        this.responseToString = responseToString;
        this.order = order;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    private ChatClientRequest before(ChatClientRequest request) {
        log.info("ai request: {}", this.requestToString.apply(request));
        return request;
    }

    private void observeAfter(ChatClientResponse response) {
        log.info("ai response: {}", this.responseToString.apply(response.chatResponse()));
    }


    private void fillTrace(Map<String, Object> adviseContext) {
        if (MDC.get("traceId") == null) {
            String traceId = (String) adviseContext.getOrDefault("traceId", "unknown");
            String spanId = (String) adviseContext.getOrDefault("spanId", "unknown");
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
        }
    }

    private void cleanTrace() {
        MDC.remove("traceId");
        MDC.remove("spanId");
    }

    @Override
    public String toString() {
        return TraceLoggerAdvisor.class.getSimpleName();
    }


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        chatClientRequest = before(chatClientRequest);

        ChatClientResponse chatClientResponse = chain.nextCall(chatClientRequest);

        observeAfter(chatClientResponse);

        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        try {
            // 为before设置MDC跟踪信息
            fillTrace(chatClientRequest.context());
            chatClientRequest = before(chatClientRequest);
        } finally {
            // 清除MDC跟踪信息
            cleanTrace();
        }

        Flux<ChatClientResponse> chatClientResponse = chain.nextStream(chatClientRequest);

        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponse, response -> {
            try {
                // 为after设置MDC跟踪信息
                fillTrace(response.context());
                observeAfter(response);
            } finally {
                // 清除MDC跟踪信息
                cleanTrace();
            }
        });
    }
}