# Nacos Prompt 示例项目

基于 `spring-ai-alibaba-starter-nacos-prompt` 实现的 Prompt 模板动态管理示例，演示如何从 Nacos 配置中心获取提示词模板并调用大模型。

## 项目结构

```
01-nacos-prompt
├── src/main/java/com/example/wx
│   └── controller
│       └── PromptController.java    # 提示词接口控制器
├── src/main/resources
│   └── application.yaml             # 应用配置
├── pom.xml
└── README.md
```

## 技术栈

- JDK 17
- Spring Boot 3.x
- Spring AI Alibaba
- Nacos 配置中心
- DashScope 大模型

## 快速开始

### 1. 环境准备

- 启动 Nacos Server（默认地址：`127.0.0.1:8848`）
- 配置环境变量 `DASH_SCOPE_API_KEY`（阿里云 DashScope API Key）

### 2. Nacos 配置

在 Nacos 控制台创建以下配置：

| 配置项 | 值 |
|--------|-----|
| **DataId** | `spring.ai.alibaba.configurable.prompt` |
| **Group** | `DEFAULT_GROUP` |
| **格式** | JSON |

**配置内容：**

```json
[
  {
    "name": "chat-greeting",
    "template": "你是一个友好的AI助手。用户 {name} 刚刚登录系统，请用热情、专业的方式向他/她打招呼，并简要介绍你能提供的帮助。回复要简洁有温度，不超过100字。",
    "model": {}
  },
  {
    "name": "summary-template",
    "template": "请对以下内容进行总结：{content}",
    "model": {}
  }
]
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:10017` 启动。

## API 接口

### 1. 聊天问候接口

从 Nacos 获取 `chat-greeting` 提示词模板，生成个性化问候语后调用大模型。

**请求：**
```
GET /nacos/greeting?name={用户名}
```

**参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | String | 否 | wangx | 用户名称 |

**示例：**
```bash
curl "http://localhost:10017/nacos/greeting?name=张三"
```

**响应：** 流式返回大模型生成的问候内容

---

### 2. 作者书籍查询接口

使用代码创建的模板查询指定作者的著名书籍。

**请求：**
```
GET /nacos/books?author={作者名}
```

**参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| author | String | 否 | 鲁迅 | 作者名称 |

**示例：**
```bash
curl "http://localhost:10017/nacos/books?author=莫言"
```

**响应：** 流式返回大模型生成的书籍列表

---

### 3. 模板渲染接口

直接获取并渲染 Nacos 中的模板（不调用大模型）。

**请求：**
```
GET /nacos/template?name={模板名称}
```

**参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | String | 否 | summary-template | 模板名称 |

**示例：**
```bash
curl "http://localhost:10017/nacos/template?name=chat-greeting"
```

**响应：** 返回渲染后的模板内容

## 核心代码说明

### ConfigurablePromptTemplateFactory

提示词模板工厂，提供两种使用方式：

```java
// 方式一：从 Nacos 获取已配置的模板
ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate("chat-greeting");

// 方式二：代码创建模板（会缓存到工厂中）
ConfigurablePromptTemplate template = promptTemplateFactory.create(
    "template-name",
    "模板内容 {variable}"
);
```

### 模板渲染与 Prompt 创建

```java
// 创建 Prompt 对象（用于调用大模型）
Prompt prompt = template.create(Map.of("name", "张三"));

// 仅渲染模板（返回字符串）
String rendered = template.render(Map.of("name", "张三"));
```

## 配置说明

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASH_SCOPE_API_KEY}  # DashScope API Key
    nacos:
      prompt:
        template:
          enabled: true               # 启用 Nacos Prompt 支持
  nacos:
    server-addr: 127.0.0.1:8848       # Nacos 服务地址
    config:
      username: nacos                  # Nacos 用户名
      password: nacos                  # Nacos 密码
```

## 注意事项

1. **DataId 固定**：当前版本 `spring-ai-alibaba-starter-nacos-prompt` 的 DataId 硬编码为 `spring.ai.alibaba.configurable.prompt`，Group 为 `DEFAULT_GROUP`，不可配置
2. **配置格式**：必须使用 JSON 数组格式
3. **动态更新**：Nacos 配置变更后会自动更新模板缓存，无需重启应用
4. **流式响应**：所有大模型调用接口均采用流式返回，支持实时输出
