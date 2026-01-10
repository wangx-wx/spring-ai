// package com.example.wx.domain.sse;
//
// import com.example.wx.domain.ChatResult;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.MediaType;
// import org.springframework.http.codec.ServerSentEvent;
// import reactor.core.publisher.Flux;
//
// import java.time.Duration;
// import java.util.Map;
// import java.util.concurrent.atomic.AtomicLong;
//
// /**
//  * SSE事件构建器
//  * 用于构建Spring WebFlux的ServerSentEvent响应
//  *
//  * @author wangx
//  * @create 2026/1/10
//  */
// @Slf4j
// public final class SseEventBuilder {
//
//     private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//
//     private SseEventBuilder() {
//         throw new UnsupportedOperationException("Utility class cannot be instantiated");
//     }
//
//     /**
//      * 将SseEvent转换为ServerSentEvent
//      */
//     @SuppressWarnings("unchecked")
//     public static ServerSentEvent<String> build(SseEvent sseEvent) {
//         ServerSentEvent.Builder<String> builder = ServerSentEvent.builder();
//
//         if (sseEvent.getEvent() != null) {
//             builder.event(sseEvent.getEvent());
//         }
//         if (sseEvent.getId() != null) {
//             builder.id(sseEvent.getId());
//         }
//         if (sseEvent.getRetry() != null) {
//             builder.retry(Duration.ofMillis(sseEvent.getRetry()));
//         }
//         if (sseEvent.getData() != null) {
//             String data = serializeData(sseEvent.getData());
//             builder.data(data);
//         }
//
//         return builder.build();
//     }
//
//     /**
//      * 将ChatResult转换为ServerSentEvent的Flux
//      */
//     public static Flux<ServerSentEvent<String>> fromChatResult(ChatResult chatResult) {
//         SseEvent sseEvent = SseEvent.fromChatResult(chatResult);
//         return Flux.just(build(sseEvent));
//     }
//
//     /**
//      * 将ChatResult流转换为ServerSentEvent的Flux
//      */
//     public static Flux<ServerSentEvent<String>> fromChatResultStream(Flux<ChatResult> chatResultStream) {
//         return chatResultStream.map(SseEvent::fromChatResult)
//                                .map(SseEventBuilder::build);
//     }
//
//     /**
//      * 创建心跳流
//      *
//      * @param interval 心跳间隔（秒）
//      */
//     public static Flux<ServerSentEvent<String>> heartbeat(long interval) {
//         return Flux.interval(Duration.ofSeconds(interval))
//                    .map(tick -> build(SseEvent.heartbeat()));
//     }
//
//     /**
//      * 创建带心跳的消息流
//      *
//      * @param messageStream 消息流
//      * @param heartbeatInterval 心跳间隔（秒）
//      */
//     public static Flux<ServerSentEvent<String>> withHeartbeat(
//             Flux<ServerSentEvent<String>> messageStream,
//             long heartbeatInterval) {
//         Flux<ServerSentEvent<String>> heartbeatStream = heartbeat(heartbeatInterval);
//         return Flux.merge(messageStream, heartbeatStream);
//     }
//
//     /**
//      * 创建文本流式响应
//      *
//      * @param textStream 文本字符流
//      * @param eventIdGenerator 事件ID生成器
//      */
//     public static Flux<ServerSentEvent<String>> streamingText(
//             Flux<String> textStream,
//             AtomicLong eventIdGenerator) {
//         return textStream.map(text -> build(SseEvent.delta(text).withId(String.valueOf(eventIdGenerator.incrementAndGet()))))
//                         .concatWith(build(SseEvent.end()));
//     }
//
//     /**
//      * 创建错误事件
//      */
//     public static ServerSentEvent<String> error(String errorMessage, Throwable cause) {
//         log.error("SSE error: {}", errorMessage, cause);
//         String errorData = serializeData(Map.of("message", errorMessage, "cause", cause != null ? cause.getMessage() : ""));
//         return ServerSentEvent.builder()
//                 .event(SseEventType.ERROR.getValue())
//                 .data(errorData)
//                 .build();
//     }
//
//     /**
//      * 序列化数据对象为JSON字符串
//      */
//     private static String serializeData(Object data) {
//         if (data == null) {
//             return "";
//         }
//         if (data instanceof String) {
//             return (String) data;
//         }
//         try {
//             return OBJECT_MAPPER.writeValueAsString(data);
//         } catch (JsonProcessingException e) {
//             log.warn("Failed to serialize data: {}", data, e);
//             return String.valueOf(data);
//         }
//     }
//
//     /**
//      * 构建SSE响应头配置
//      */
//     public static MediaType getSseMediaType() {
//         return MediaType.parseMediaType("text/event-stream");
//     }
// }
