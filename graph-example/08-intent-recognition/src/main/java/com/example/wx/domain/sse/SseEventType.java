// package com.example.wx.domain.sse;
//
// /**
//  * SSE事件类型枚举
//  *
//  * @author wangx
//  * @create 2026/1/10
//  */
// public enum SseEventType {
//
//     /**
//      * 文本内容事件
//      */
//     TEXT("text"),
//
//     /**
//      * 选项事件
//      */
//     OPTION("option"),
//
//     /**
//      * 流式增量事件
//      */
//     DELTA("delta"),
//
//     /**
//      * 结束事件
//      */
//     END("end"),
//
//     /**
//      * 错误事件
//      */
//     ERROR("error"),
//
//     /**
//      * 心跳事件
//      */
//     HEARTBEAT("heartbeat"),
//
//     /**
//      * 状态事件
//      */
//     STATUS("status");
//
//     private final String value;
//
//     SseEventType(String value) {
//         this.value = value;
//     }
//
//     public String getValue() {
//         return value;
//     }
//
//     public static SseEventType fromValue(String value) {
//         for (SseEventType type : values()) {
//             if (type.value.equals(value)) {
//                 return type;
//             }
//         }
//         return STATUS;
//     }
// }