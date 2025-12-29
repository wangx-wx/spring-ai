package com.example.wx.controller;

import io.milvus.client.MilvusClient;
import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CollectionSchemaParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wangx
 * @description
 * @create 2025/5/26 09:50
 */
@RestController
public class EmbeddingController {
    @Resource
    private MilvusVectorStore milvusVectorStore;

    @Resource
    private MilvusClient milvusClient;

    @Resource
    private EmbeddingModel embeddingModel;



    @PostConstruct
    public void init() {
        List<FieldType> fieldTypes = new ArrayList<>();
        fieldTypes.add(FieldType.newBuilder().withName("doc_id").withDataType(DataType.VarChar).withMaxLength(128).withPrimaryKey(true).build());
        fieldTypes.add(FieldType.newBuilder().withName("embedding").withDataType(DataType.FloatVector).withDimension(1536).build());
        fieldTypes.add(FieldType.newBuilder().withName("content").withDataType(DataType.VarChar).withMaxLength(1024).build());
        fieldTypes.add(FieldType.newBuilder().withName("metadata").withDataType(DataType.JSON).build());

        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName("vector_store")
                .withFieldName("embedding")
                .withIndexType(IndexType.AUTOINDEX)
                .withMetricType(MetricType.COSINE)
                .withSyncMode(true)
                .build();
        // 构建 schema
        CollectionSchemaParam schema = CollectionSchemaParam.newBuilder()
                .withFieldTypes(fieldTypes)
                .build();

        // 构建创建 Collection 请求
        CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName("vector_store")
                .withShardsNum(2)                  // 可配置分片数
                .withSchema(schema)
                .build();

        // 创建 Collection
        R<RpcStatus> response = milvusClient.createCollection(createCollectionParam);

        milvusClient.createIndex(indexParam);
        if (response.getStatus() != R.Status.Success.getCode()) {
            System.err.println("创建 Collection 失败：" + response.getMessage());
        } else {
            System.out.println("Collection 创建成功！");
        }
        LoadCollectionParam loadParam = LoadCollectionParam.newBuilder()

                .withCollectionName("vector_store")
                .build();

        milvusClient.loadCollection(loadParam);
    }


    @GetMapping("/add")
    public String add() {


        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

        // Add the documents to Milvus Vector Store
        milvusVectorStore.add(documents);

        List<Document> results = this.milvusVectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        return results.toString();
    }
}
