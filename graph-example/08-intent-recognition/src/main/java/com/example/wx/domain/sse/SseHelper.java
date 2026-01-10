// package com.example.wx.domain.sse;
//
// import com.example.wx.domain.ChatResult;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.codec.ServerSentEvent;
// import org.springframework.web.reactive.function.server.ServerResponse;
// import reactor.core.publisher.Flux;
// import reactor.core.publisher.Mono;
//
// import java.time.Duration;
// import java.util.concurrent.atomic.AtomicLong;
//
// /**
//  * SSE工具类
//  * 推荐直接返回 Flux<ChatResult>，Spring会自动序列化
//  * 如需高级功能（event字段、断线重连等），可使用本类方法
//  *
//  * @author wangx
//  * @create 2026/1/10
//  */
// @Slf4j
// public final class SseHelper {
//
//     private static final AtomicLong EVENT_ID_GENERATOR = new AtomicLong(0);
//
//     private SseHelper() {
//         throw new UnsupportedOperationException("Utility class cannot be instantiated");
//     }
//
//     // ========== 简化方案：直接返回 Flux<ChatResult> ==========
//
//     /**
//      * 推荐方式：直接返回 Flux<ChatResult>
//      * Spring会自动序列化为 SSE 格式
//      *
//      * 使用示例：
//      * <pre>
//      * @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//      * public Flux<ChatResult> stream(@RequestParam String query) {
//      *     return chatService.process(query);
//      * }
//      * </pre>
//      */
//     public static Flux<ChatResult> direct(Flux<ChatResult> chatResultStream) {
//         return chatResultStream;
//     }
//
//     // ========== 高级方案：使用 ServerSentEvent ==========
//
//     /**
//      * 创建单个ChatResult的SSE响应
//      *
//      * @param chatResult 聊天结果
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> of(ChatResult chatResult) {
//         return SseEventBuilder.fromChatResult(chatResult);
//     }
//
//     /**
//      * 创建ChatResult流的SSE响应
//      *
//      * @param chatResultStream 聊天结果流
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> of(Flux<ChatResult> chatResultStream) {
//         return SseEventBuilder.fromChatResultStream(chatResultStream);
//     }
//
//     /**
//      * 创建文本内容的SSE响应
//      *
//      * @param content 文本内容
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> text(String content) {
//         return Flux.just(SseEventBuilder.build(SseEvent.text(content)));
//     }
//
//     /**
//      * 创建文本流式SSE响应
//      *
//      * @param contentStream 文本字符流
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> streamingText(Flux<String> contentStream) {
//         return SseEventBuilder.streamingText(contentStream, EVENT_ID_GENERATOR);
//     }
//
//     /**
//      * 创建选项SSE响应
//      *
//      * @param options 选项列表
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> options(ChatResult.Option... options) {
//         return Flux.just(SseEventBuilder.build(SseEvent.option(options[0])))
//                    .concatWith(Flux.fromArray(options)
//                                           .map(SseEvent::option)
//                                           .map(SseEventBuilder::build));
//     }
//
//     /**
//      * 创建结束事件
//      *
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> end() {
//         return Flux.just(SseEventBuilder.build(SseEvent.end()));
//     }
//
//     /**
//      * 创建错误事件
//      *
//      * @param errorMessage 错误消息
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> error(String errorMessage) {
//         return Flux.just(SseEventBuilder.build(SseEvent.error(errorMessage)));
//     }
//
//     /**
//      * 创建错误事件（带异常）
//      *
//      * @param errorMessage 错误消息
//      * @param cause        异常原因
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> error(String errorMessage, Throwable cause) {
//         return Flux.just(SseEventBuilder.error(errorMessage, cause));
//     }
//
//     /**
//      * 创建状态事件
//      *
//      * @param status 状态描述
//      * @return SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> status(String status) {
//         return Flux.just(SseEventBuilder.build(SseEvent.status(status)));
//     }
//
//     /**
//      * 创建带心跳的SSE响应
//      *
//      * @param messageStream    消息流
//      * @param heartbeatSeconds 心跳间隔（秒）
//      * @return 带心跳的SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> withHeartbeat(
//             Flux<ServerSentEvent<String>> messageStream,
//             long heartbeatSeconds) {
//         return SseEventBuilder.withHeartbeat(messageStream, heartbeatSeconds);
//     }
//
//     /**
//      * 创建默认心跳间隔（30秒）的SSE响应
//      *
//      * @param messageStream 消息流
//      * @return 带心跳的SSE事件流
//      */
//     public static Flux<ServerSentEvent<String>> withHeartbeat(Flux<ServerSentEvent<String>> messageStream) {
//         return withHeartbeat(messageStream, 30);
//     }
//
//     /**
//      * 构建ServerResponse（用于函数式路由）
//      *
//      * @param sseFlux SSE事件流
//      * @return ServerResponse
//      */
//     public static Mono<ServerResponse> toServerResponse(Flux<ServerSentEvent<String>> sseFlux) {
//         return ServerResponse.ok()
//                 .contentType(SseEventBuilder.getSseMediaType())
//                 .body(sseFlux, ServerSentEvent.class);
//     }
//
//     /**
//      * 处理异常并返回错误事件
//      *
//      * @param throwable 异常
//      * @return 错误事件流
//      */
//     public static Flux<ServerSentEvent<String>> handleException(Throwable throwable) {
//         log.error("SSE exception occurred", throwable);
//         String message = throwable.getMessage() != null ? throwable.getMessage() : "Internal server error";
//         return error(message, throwable)
//                 .delayElements(Duration.ofMillis(100));
//     }
//
//     /**
//      * 为Flux添加错误处理
//      *
//      * @param flux 原始流
//      * @return 带错误处理的流
//      */
//     public static Flux<ServerSentEvent<String>> withErrorHandling(
//             Flux<ServerSentEvent<String>> flux) {
//         return flux.onErrorResume(SseHelper::handleException);
//     }
//
//     /**
//      * 生成唯一事件ID
//      *
//      * @return 事件ID
//      */
//     public static String generateEventId() {
//         return String.valueOf(EVENT_ID_GENERATOR.incrementAndGet());
//     }
//
//     /**
//      * 生成基于会话的事件ID
//      *
//      * @param sessionId 会话ID
//      * @return 事件ID
//      */
//     public static String generateEventId(String sessionId) {
//         return sessionId + "-" + EVENT_ID_GENERATOR.incrementAndGet();
//     }
// }