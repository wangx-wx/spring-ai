// package com.example.wx.domain.sse;
//
// import com.example.wx.domain.ChatResult;
// import com.fasterxml.jackson.annotation.JsonProperty;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;
//
// import java.util.Map;
//
// /**
//  * SSE服务端推送事件封装
//  * 符合W3C SSE规范: https://html.spec.whatwg.org/multipage/server-sent-events.html
//  *
//  * @author wangx
//  * @create 2026/1/10
//  */
// @Data
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
// public class SseEvent {
//
//     /**
//      * 事件类型，对应SSE的event字段
//      * 客户端可通过addEventListener监听特定事件类型
//      */
//     @JsonProperty("event")
//     private String event;
//
//     /**
//      * 事件ID，对应SSE的id字段
//      * 用于客户端断线重连时恢复，Last-Event-ID header会被发送回服务器
//      */
//     @JsonProperty("id")
//     private String id;
//
//     /**
//      * 重连间隔（毫秒），对应SSE的retry字段
//      * 客户端会在连接断开后等待指定时间后尝试重连
//      */
//     @JsonProperty("retry")
//     private Long retry;
//
//     /**
//      * 事件数据，对应SSE的data字段
//      * 必须是JSON可序列化对象
//      */
//     @JsonProperty("data")
//     private Object data;
//
//     /**
//      * 时间戳，用于客户端排序和调试
//      */
//     @JsonProperty("timestamp")
//     private Long timestamp;
//
//     /**
//      * 创建文本事件
//      */
//     public static SseEvent text(String content) {
//         return SseEvent.builder()
//                 .event(SseEventType.TEXT.getValue())
//                 .data(content)
//                 .timestamp(System.currentTimeMillis())
//                 .build();
//     }
//
//     /**
//      * 创建选项事件
//      */
//     public static SseEvent option(ChatResult.Option option) {
//         return SseEvent.builder()
//                 .event(SseEventType.OPTION.getValue())
//                 .data(option)
//                 .timestamp(System.currentTimeMillis())
//                 .build();
//     }
//
//     /**
//      * 创建增量流式事件
//      */
//     public static SseEvent delta(String deltaContent) {
//         return SseEvent.builder()
//                 .event(SseEventType.DELTA.getValue())
//                 .data(deltaContent)
//                 .timestamp(System.currentTimeMillis())
//                 .build();
//     }
//
//     /**
//      * 创建结束事件
//      */
//     public static SseEvent end() {
//         return SseEvent.builder()
//                 .event(SseEventType.END.getValue())
//                 .data("[DONE]")
//                 .timestamp(System.currentTimeMillis())
//                 .build();
//     }
//
//     /**
//      * 创建错误事件
//      */
//     public static SseEvent error(String errorMessage) {
//         return SseEvent.builder()
//                 .event(SseEventType.ERROR.getValue())
//                 .data(Map.of("message", errorMessage, "code", "ERROR"))
//                 .timestamp(System.currentTimeMillis())
//                 .build();
//     }
//
//     /**
//      * 创建心跳事件
//      */
//     public static SseEvent heartbeat() {
//         return SseEvent.builder()
//                 .event(SseEventType.HEARTBEAT.getValue())
//                 .data("ping")
//                 .timestamp(System.currentTimeMillis())
//                 .build();
//     }
//
//     /**
//      * 创建状态事件
//      */
//     public static SseEvent status(String status) {
//         return SseEvent.builder()
//                 .event(SseEventType.STATUS.getValue())
//                 .data(Map.of("status", status))
//                 .timestamp(System.currentTimeMillis())
//                 .build();
//     }
//
//     /**
//      * 从ChatResult转换为SSE事件
//      */
//     public static SseEvent fromChatResult(ChatResult chatResult) {
//         return fromChatResult(chatResult, null);
//     }
//
//     /**
//      * 从ChatResult转换为SSE事件（带ID）
//      */
//     public static SseEvent fromChatResult(ChatResult chatResult, String eventId) {
//         SseEventBuilder builder = SseEvent.builder()
//                 .id(eventId)
//                 .timestamp(System.currentTimeMillis());
//
//         switch (chatResult.getType()) {
//             case "text":
//                 builder.event(SseEventType.TEXT.getValue())
//                        .data(chatResult.getContent());
//                 break;
//             case "option":
//                 builder.event(SseEventType.OPTION.getValue())
//                        .data(chatResult.getOptions());
//                 break;
//             case "end":
//                 builder.event(SseEventType.END.getValue())
//                        .data("[DONE]");
//                 break;
//             case "error":
//                 builder.event(SseEventType.ERROR.getValue())
//                        .data(Map.of("message", chatResult.getContent(), "code", "ERROR"));
//                 break;
//             default:
//                 builder.event(SseEventType.STATUS.getValue())
//                        .data(chatResult.getContent());
//         }
//
//         return builder.build();
//     }
//
//     /**
//      * 构建带事件ID的事件
//      */
//     public SseEvent withId(String id) {
//         this.setId(id);
//         return this;
//     }
//
//     /**
//      * 构建带重连间隔的事件
//      */
//     public SseEvent withRetry(Long retry) {
//         this.setRetry(retry);
//         return this;
//     }
//
//     /**
//      * 转换为SSE文本格式（用于调试）
//      */
//     public String toSseText() {
//         StringBuilder sb = new StringBuilder();
//         if (event != null) {
//             sb.append("event: ").append(event).append("\n");
//         }
//         if (id != null) {
//             sb.append("id: ").append(id).append("\n");
//         }
//         if (retry != null) {
//             sb.append("retry: ").append(retry).append("\n");
//         }
//         if (data != null) {
//             // 简单处理，实际应使用JSON序列化
//             sb.append("data: ").append(data).append("\n");
//         }
//         sb.append("\n");
//         return sb.toString();
//     }
// }
