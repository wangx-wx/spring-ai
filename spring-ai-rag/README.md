# Spring AI RAG

基于 Spring AI 框架的检索增强生成（Retrieval-Augmented Generation）示例项目，展示了多种向量数据库和文档处理方案的集成实践。

## 项目简介

本项目是一个多模块的 Spring Boot 应用，旨在演示如何使用 Spring AI 构建 RAG（检索增强生成）系统。通过整合阿里云百炼大模型、Milvus、PgVector 等技术，实现文档导入、向量化存储、相似性搜索和智能问答等功能。

RAG 技术通过检索外部知识库中的相关文档，结合大语言模型（LLM）生成更准确、更有依据的回答，有效解决了 LLM 的知识截止和幻觉问题。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.7 | 基础框架 |
| Spring AI | 1.1.0 | AI 集成框架 |
| Spring AI Alibaba | 1.1.0.0-RC2 | 阿里云百炼集成 |
| Milvus | - | 向量数据库 |
| PostgreSQL + PgVector | - | 向量扩展数据库 |
| Apache Tika | - | 多格式文档解析 |

## 项目结构

```
spring-ai-rag/
├── pom.xml                          # 父模块配置
├── rag-bailian-knowledge/           # 阿里云百炼知识库模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/wx/
│       │   ├── KnowledgeApplication.java      # 应用入口
│       │   ├── config/BaiLianConfig.java      # 百炼API配置
│       │   ├── controller/CloudRagController.java
│       │   └── service/
│       │       ├── RagService.java
│       │       └── impl/RagServiceImpl.java
│       └── resources/
│           ├── application.yaml
│           └── data/                          # 示例文档
├── rag-etl/                         # ETL文档处理模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/wx/
│       │   ├── RagETLApplication.java
│       │   ├── controller/ReaderController.java
│       │   └── model/Constant.java
│       └── resources/
│           ├── application.yaml
│           └── data/                          # 多格式示例文档
├── rag-milvus/                      # Milvus向量数据库模块
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/wx/
│       │   ├── RagMilvusApplication.java
│       │   ├── config/MilvusConfig.java
│       │   └── controller/EmbeddingController.java
│       └── resources/application.yml
└── rag-pgvector/                    # PgVector向量数据库模块
    ├── pom.xml
    └── src/main/
        ├── java/com/example/wx/
        │   ├── RagPgVectorApplication.java
        │   ├── controller/
        │   │   ├── KnowledgeBaseController.java
        │   │   └── RagPgVectorController.java
        │   └── service/
        │       ├── KnowledgeBaseService.java
        │       └── KnowledgeBaseServiceImpl.java
        └── resources/
            ├── application.yml
            ├── data/                          # PDF示例文档
            └── prompts/system-qa.st           # 系统提示模板
```

## 模块说明

### 1. rag-bailian-knowledge

**阿里云百炼云端知识库模块**

利用阿里云百炼（DashScope）提供的云端向量存储和检索服务，无需自建向量数据库。

**主要功能：**
- 文档上传至百炼云端知识库
- 基于云端知识库的文档检索
- 流式问答生成

**端口：** 10019

### 2. rag-etl

**ETL 文档处理模块**

展示 Spring AI 对多种文档格式的解析能力，是 RAG 流程中"Extract-Transform-Load"环节的示例。

**支持的文档格式：**
- 纯文本（TXT）
- JSON
- Markdown
- PDF（按页/按段落）
- HTML
- 通用格式（通过 Apache Tika）

**端口：** 10016

### 3. rag-milvus

**Milvus 向量数据库模块**

使用 Milvus 作为向量存储后端，适合大规模向量检索场景。

**主要功能：**
- 文档向量化存储
- 相似性搜索
- Collection 自动创建和索引配置

**端口：** 10010

### 4. rag-pgvector

**PostgreSQL + PgVector 模块**

使用 PostgreSQL 的 PgVector 扩展作为向量存储，适合已有 PostgreSQL 基础设施的场景。

**主要功能：**
- 文本内容插入向量库
- 文件上传（支持 PDF、Word、TXT 等）
- 相似性搜索
- 阻塞式/流式知识库问答
- Rerank 重排序优化

**端口：** 10011

## 核心依赖

| 依赖名称 | 用途 |
|---------|------|
| spring-ai-alibaba-starter-dashscope | 阿里云百炼大模型集成 |
| spring-ai-rag | RAG 核心功能 |
| spring-ai-milvus-store | Milvus 向量存储集成 |
| spring-ai-pgvector-store | PgVector 向量存储集成 |
| spring-ai-pdf-document-reader | PDF 文档解析 |
| spring-ai-tika-document-reader | 通用文档解析（Tika） |
| spring-ai-jsoup-document-reader | HTML 文档解析 |
| spring-ai-markdown-document-reader | Markdown 文档解析 |

## 配置说明

### 环境变量

在项目根目录创建 `.env` 文件：

```properties
# 阿里云百炼 API Key
DASH_SCOPE_API_KEY=your_dashscope_api_key
```

### Milvus 配置（rag-milvus）

```yaml
spring:
  ai:
    vectorstore:
      milvus:
        client:
          host: 192.168.3.23      # Milvus 服务地址
          port: 19530             # Milvus 服务端口
        databaseName: "default"
        collectionName: "test_vector_store"
        embeddingDimension: 1536  # 向量维度
        indexType: IVF_FLAT       # 索引类型
        metricType: COSINE        # 相似度度量
        initialize-schema: true   # 自动创建 Schema
```

### PgVector 配置（rag-pgvector）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/db
    username: pgsql
    password: 123456
  ai:
    dashscope:
      embedding:
        options:
          model: text-embedding-v4
          dimensions: 1024
    vectorstore:
      pgvector:
        dimensions: 1024
        index-type: hnsw
        table-name: mxy_rag_vector
        initialize-schema: true
```

### 大模型配置

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASH_SCOPE_API_KEY}
      chat:
        options:
          model: qwen-plus-2025-01-25  # 通义千问模型
          temperature: 0.7
```

## 快速开始

### 前置条件

1. JDK 17+
2. Maven 3.6+
3. 阿里云百炼 API Key
4. （可选）Milvus 服务
5. （可选）PostgreSQL + PgVector 扩展

### 运行步骤

1. **克隆项目并配置环境变量**

   ```bash
   cd spring-ai-rag
   # 创建 .env 文件并填入 API Key
   echo "DASH_SCOPE_API_KEY=your_api_key" > .env
   ```

2. **编译项目**

   ```bash
   mvn clean install -DskipTests
   ```

3. **运行指定模块**

   ```bash
   # 运行 ETL 模块（文档解析示例）
   cd rag-etl
   mvn spring-boot:run

   # 运行 PgVector 模块（需要 PostgreSQL）
   cd rag-pgvector
   mvn spring-boot:run

   # 运行 Milvus 模块（需要 Milvus 服务）
   cd rag-milvus
   mvn spring-boot:run

   # 运行百炼云端知识库模块
   cd rag-bailian-knowledge
   mvn spring-boot:run
   ```

## API 接口

### rag-etl 模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/reader/text` | GET | 读取 TXT 文件 |
| `/reader/json` | GET | 读取 JSON 文件 |
| `/reader/pdf-page` | GET | 按页读取 PDF |
| `/reader/pdf-paragraph` | GET | 按段落读取 PDF |
| `/reader/markdown` | GET | 读取 Markdown 文件 |
| `/reader/html` | GET | 读取 HTML 文件 |
| `/reader/tika` | GET | 使用 Tika 读取文件 |

### rag-pgvector 模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/knowledge-base/insert-text` | GET | 插入文本到向量库 |
| `/api/v1/knowledge-base/upload-file` | POST | 上传文件到知识库 |
| `/api/v1/knowledge-base/search` | GET | 相似性搜索 |
| `/api/v1/knowledge-base/chat` | GET | 知识库问答（阻塞式） |
| `/api/v1/knowledge-base/chat-stream` | GET | 知识库问答（流式） |
| `/ai/rag/import/docs` | GET | 导入示例 PDF 文档 |
| `/ai/rag` | GET | RAG 问答（带 Rerank） |

### rag-bailian-knowledge 模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/ai/knowledge/import` | GET | 导入文档到百炼云端 |
| `/ai/knowledge/generate` | GET | 基于知识库生成回答 |
| `/ai/delete` | GET | 删除知识库文档 |

### rag-milvus 模块

| 接口 | 方法 | 说明 |
|------|------|------|
| `/add` | GET | 添加文档并执行相似性搜索 |

## 开发指南

### RAG 流程概述

1. **Extract（提取）**：使用 DocumentReader 解析各种格式的文档
2. **Transform（转换）**：使用 TextSplitter 将文档分割成适当大小的块
3. **Load（加载）**：将文档块通过 EmbeddingModel 转换为向量，存入 VectorStore
4. **Retrieve（检索）**：根据用户查询进行相似性搜索
5. **Generate（生成）**：将检索结果作为上下文，结合 LLM 生成回答

### 扩展开发

- **添加新的文档类型支持**：实现 `DocumentReader` 接口
- **集成新的向量数据库**：实现 `VectorStore` 接口
- **自定义文本分割策略**：扩展 `TextSplitter` 类

## 许可证

本项目仅供学习和参考使用。
