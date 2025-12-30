# spring-ai-alibaba-starter-nacos-prompt 使用指南

## 功能概述

`spring-ai-alibaba-starter-nacos-prompt` 是一个 Spring Boot Starter，用于将 **Prompt 模板存储在 Nacos 配置中心**，实现动态的 Prompt 管理和版本控制。

### 核心特性

- 将 Prompt 模板集中存储在 Nacos 配置中心
- 支持动态更新，无需重启应用
- 模板缓存机制，提高性能
- 支持变量替换和模型绑定

---

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-nacos-prompt</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2. 配置文件

```yaml
spring:
  ai:
    nacos:
      prompt:
        template:
          enabled: true  # 启用 Nacos Prompt 支持
  nacos:
    config:
      server-addr: localhost:8848
      namespace: your-namespace-id
```

### 3. 在 Nacos 中配置 Prompt 模板

**配置信息：**

| 配置项 | 值 |
|---|---|
| **DataId** | `spring.ai.alibaba.configurable.prompt` |
| **Group** | `DEFAULT_GROUP` |
| **格式** | JSON 数组 |

**配置示例：**

```json
[
  {
    "name": "chat-greeting",
    "template": "你好，{name}！欢迎使用 {app_name}。",
    "model": {
      "app_name": "Spring AI 助手"
    }
  },
  {
    "name": "code-review",
    "template": "请评审以下 {language} 代码：\n{code}",
    "model": {}
  }
]
```

---

## 代码使用示例

### 基本使用

```java
@Service
public class ChatService {

    @Autowired
    private ConfigurablePromptTemplateFactory promptFactory;

    public String chat(String userName) {
        // 获取模板
        ConfigurablePromptTemplate template = promptFactory.get("chat-greeting");

        // 渲染模板
        Map<String, Object> model = new HashMap<>();
        model.put("name", userName);

        return template.render(model);
    }
}
```

### 结合 ChatClient 使用

```java
@Service
public class AiChatService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ConfigurablePromptTemplateFactory promptFactory;

    public String generateResponse(String userMessage) {
        // 从 Nacos 获取动态 Prompt
        ConfigurablePromptTemplate promptTemplate = promptFactory.get("chat-prompt");

        // 构建 Prompt
        Prompt prompt = promptTemplate.create(Map.of("input", userMessage));

        // 调用 AI
        return chatClient.call(prompt.content());
    }
}
```

### 手动创建模板

```java
@Configuration
public class PromptConfig {

    @Bean
    public ConfigurablePromptTemplate customPrompt(
            ConfigurablePromptTemplateFactory factory) {

        String templateContent = "分析以下文本：{text}";
        return factory.create("analyzer", templateContent);
    }
}
```

---

## 核心 API

| 类 | 作用 |
|---|---|
| `ConfigurablePromptTemplateFactory` | Prompt 模板工厂，管理模板创建和缓存 |
| `ConfigurablePromptTemplate` | 可配置的 Prompt 模板实例 |
| `PromptTemplateCustomizer` | 模板自定义器接口 |

---

## 高级用法

### 自定义 Prompt 模板

```java
@Bean
public PromptTemplateCustomizer promptCustomizer() {
    return builder -> builder
        .variables(Map.of(
            "system", "你是一个专业的AI助手",
            "language", "中文"
        ))
        .renderer(new CustomRenderer());
}
```

### 动态监听配置变更

当 Nacos 中的配置变更时，应用会自动更新模板缓存，无需重启。

---

## 配置属性

| 属性 | 默认值 | 说明 |
|---|---|---|
| `spring.ai.nacos.prompt.template.enabled` | `false` | 是否启用 Nacos Prompt 支持 |

---

## 配置限制说明

### 硬编码配置

| 配置项 | 当前值 | 是否可配置 |
|---|---|---|
| **dataId** | `spring.ai.alibaba.configurable.prompt` | ❌ 硬编码 |
| **group** | `DEFAULT_GROUP` | ❌ 硬编码 |
| **格式** | JSON 数组 | ❌ 固定格式 |

**硬编码位置：** `ConfigurablePromptTemplateFactory.java:65`

```java
@NacosConfigListener(dataId = "spring.ai.alibaba.configurable.prompt",
                     group = "DEFAULT_GROUP",
                     initNotify = true)
protected void onConfigChange(List<ConfigurablePromptTemplateModel> configList) {
    // ...
}
```

---

## 自定义解决方案

### 方案一：继承并覆盖（推荐）

创建自定义的工厂类，使用自己的 dataId 和格式：

```java
@Component
public class CustomPromptTemplateFactory extends ConfigurablePromptTemplateFactory {

    public CustomPromptTemplateFactory(PromptTemplateBuilderConfigure configure) {
        super(configure);
    }

    // 使用自己的 dataId 和 group
    @NacosConfigListener(dataId = "my-custom-prompts",
                         group = "MY_GROUP",
                         initNotify = true)
    protected void onCustomConfigChange(String content) {
        // 自定义解析逻辑，支持其他格式（如 YAML）
        List<MyPromptConfig> configs = parseYaml(content);

        for (MyPromptConfig config : configs) {
            create(config.name(), config.template());
        }
    }

    private List<MyPromptConfig> parseYaml(String yaml) {
        // 使用 SnakeYAML 等工具解析
        // ...
    }
}
```

### 方案二：扩展配置属性

可以向项目提交 PR，增加可配置的 dataId 和 group：

```java
@ConfigurationProperties(NacosPromptTmplProperties.TEMPLATE_PREFIX)
public class NacosPromptTmplProperties {

    private boolean enabled = false;

    // 新增配置项
    private String dataId = "spring.ai.alibaba.configurable.prompt";

    private String group = "DEFAULT_GROUP";

    // getter/setter...
}
```

### 方案三：完全自定义实现

如果需要完全不同的格式和实现，可以不使用 starter，自己实现：

```java
@Component
public class MyPromptFactory {

    @NacosConfigListener(dataId = "prompts.yaml", group = "AI_GROUP")
    public void onConfigChange(String yamlContent) {
        // 解析 YAML 格式的 Prompt 配置
        // ...
    }
}
```

---

## 解决方案总结

| 需求 | 当前支持 | 推荐方案 |
|---|---|---|
| 修改 dataId | ❌ | 方案一：继承覆盖 |
| 修改 group | ❌ | 方案一：继承覆盖 |
| 修改格式（如 YAML） | ❌ | 方案一：继承覆盖 + 自定义解析 |
| 开箱即用 | ✅ | 使用默认配置 |

---

## 源码参考

- **工厂类：** `prompt/spring-ai-alibaba-prompt-nacos/src/main/java/com/alibaba/cloud/ai/prompt/ConfigurablePromptTemplateFactory.java`
- **配置类：** `auto-configurations/spring-ai-alibaba-autoconfigure-nacos-prompt/src/main/java/com/alibaba/cloud/ai/autoconfigure/prompt/NacosPromptTmplProperties.java`
- **自动配置：** `auto-configurations/spring-ai-alibaba-autoconfigure-nacos-prompt/src/main/java/com/alibaba/cloud/ai/autoconfigure/prompt/PromptTemplateAutoConfiguration.java`
