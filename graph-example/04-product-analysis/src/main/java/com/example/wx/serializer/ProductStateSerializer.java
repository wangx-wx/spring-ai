package com.example.wx.serializer;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.serializer.plain_text.PlainTextStateSerializer;
import com.alibaba.cloud.ai.graph.state.AgentStateFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * @author wangxiang
 * @description Custom StateSerializer for Product object serialization with type information
 * @create 2025/10/25 16:54
 */
public class ProductStateSerializer  extends PlainTextStateSerializer {
    private final ObjectMapper mapper;

    public ProductStateSerializer(AgentStateFactory<OverAllState> stateFactory) {
        super(stateFactory);
        this.mapper = new ObjectMapper();
        // Enable default typing to handle custom objects like Product
        this.mapper.activateDefaultTyping(
                this.mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        // Exclude null values from serialization
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void writeData(Map<String, Object> data, ObjectOutput out) throws IOException {
        String json = mapper.writeValueAsString(data);
        out.writeUTF(json);
    }

    @Override
    public Map<String, Object> readData(ObjectInput in) throws IOException {
        String json = in.readUTF();
        return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
    }

    @Override
    public OverAllState cloneObject(OverAllState state) throws IOException {
        String json = mapper.writeValueAsString(state.data());
        Map<String, Object> rawMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
        });
        return stateFactory().apply(rawMap);
    }
}
