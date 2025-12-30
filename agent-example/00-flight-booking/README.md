# Flight Booking Agent

基于 Spring AI + 阿里云通义千问的智能航班预订客服代理示例项目。

## 项目概述

本项目演示如何使用 Spring AI 框架构建一个具备以下能力的智能对话助手：

- **多轮对话记忆**：能够记住上下文，避免重复询问用户信息
- **工具调用（Function Calling）**：AI 可以调用预定义的业务函数完成实际操作
- **RAG（检索增强生成）**：通过向量检索服务条款，确保 AI 回答符合业务规则

### 业务场景

模拟 "Funnair" 航空公司的在线客服系统，支持：

- 查询机票预订详情
- 修改预订日期/航线
- 取消机票预订

## 技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Java | 17 | JDK版本 |
| Spring Boot | 3.5.7 | 基础框架 |
| Spring AI | 1.1.0 | AI应用开发框架 |
| Spring AI Alibaba | 1.1.0.0-RC2 | 阿里云AI集成 |
| 通义千问 | qwen-plus-2025-01-25 | 大语言模型 |
| 文本向量模型 | text-embedding-v4 | 文本嵌入 |

## 项目结构

```
00-flight-booking/
├── pom.xml
└── src/main/
    ├── java/com/example/wx/
    │   ├── FlightBookingApplication.java      # 应用入口、Bean配置
    │   ├── controller/
    │   │   ├── CustomerAssistantController.java  # AI助手API
    │   │   └── FlightBookingController.java      # 预订管理API
    │   ├── domain/
    │   │   ├── Booking.java                   # 预订实体
    │   │   ├── BookingClass.java              # 舱位等级枚举
    │   │   ├── BookingData.java               # 内存数据存储
    │   │   ├── BookingStatus.java             # 预订状态枚举
    │   │   └── Customer.java                  # 客户实体
    │   └── service/
    │       ├── BookingTools.java              # AI可调用的工具函数
    │       ├── CustomerAssistantService.java  # AI对话服务
    │       └── FlightBookingService.java      # 预订业务逻辑
    └── resources/
        ├── application.yml                    # 应用配置
        └── rag/
            └── terms-of-service.txt           # 服务条款(用于RAG)
```

## 核心架构

```
用户请求
   │
   ▼
[Controller] ──► [CustomerAssistantService] ──► [ChatClient]
                                                     │
                                     ┌───────────────┼───────────────┐
                                     │               │               │
                              [ChatMemory]    [VectorStore]    [BookingTools]
                              (对话记忆)       (RAG检索)        (工具调用)
                                                                     │
                                                                     ▼
                                                         [FlightBookingService]
                                                            (业务逻辑)
```

## AI 工具函数

| 工具名 | 功能 | 参数 |
|-------|------|------|
| `getBookingDetails` | 查询预订详情 | bookingNumber, name |
| `changeBooking` | 修改预订 | bookingNumber, name, date, from, to |
| `cancelBooking` | 取消预订 | bookingNumber, name |

## API 接口

| 路径 | 方法 | 说明 |
|-----|------|------|
| `/api/assistant/chat` | GET | 同步对话接口 |
| `/api/assistant/stream/chat` | GET | 流式对话接口(SSE) |
| `/api/bookings` | GET | 获取所有预订列表 |

**请求参数**：

- `chatId`：会话ID（用于对话记忆）
- `userMessage`：用户消息

## 服务条款（RAG知识库）

`terms-of-service.txt` 定义了 Funnair 航空的服务规则：

| 规则 | 内容 |
|-----|------|
| 修改时限 | 起飞前24小时 |
| 修改费用 | 经济舱$50, 豪华经济$30, 商务舱免费 |
| 取消时限 | 起飞前48小时 |
| 取消费用 | 经济舱$75, 豪华经济$50, 商务舱$25 |
| 退款周期 | 7个工作日 |

## 快速开始

### 1. 配置环境变量

在项目根目录创建 `.env` 文件：

```properties
DASH_SCOPE_API_KEY=your_api_key_here
```

### 2. 构建运行

```bash
cd agent-example/00-flight-booking
mvn spring-boot:run
```

### 3. 测试对话

```bash
# 打招呼
curl "http://localhost:10012/api/assistant/chat?chatId=test1&userMessage=你好"

# 查询预订
curl "http://localhost:10012/api/assistant/chat?chatId=test1&userMessage=我是云小宝，帮我查一下101号订单"

# 流式对话
curl "http://localhost:10012/api/assistant/stream/chat?chatId=test1&userMessage=帮我改签到明天"
```

## 设计亮点

1. **关注点分离**：工具定义(`BookingTools`)与业务逻辑(`FlightBookingService`)解耦
2. **声明式工具注册**：使用 `@Bean` + `@Description` 注解，简洁优雅
3. **RAG增强**：AI 回答受服务条款约束，确保合规性
4. **对话记忆**：`MessageWindowChatMemory` 支持多轮对话上下文
5. **流式响应**：支持 SSE 流式输出，提升用户体验
