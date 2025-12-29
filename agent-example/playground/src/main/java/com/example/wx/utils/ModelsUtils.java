package com.example.wx.utils;

import com.example.wx.entiy.dashscope.DashScopeModel;
import com.example.wx.entiy.dashscope.DashScopeModels;
import com.example.wx.enums.ModelEnums;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 18:06
 */
public final class ModelsUtils {
    private final static String MODELS_FILE_PATH = "models.yaml";

    private static final String MODEL = "model";

    private static final String DESC = "desc";

    private ModelsUtils() {
    }

    public static Set<Map<String, String>> getDashScopeModels() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream resourceAsStream = ModelsUtils.class.getClassLoader().getResourceAsStream(MODELS_FILE_PATH);
        DashScopeModels models = mapper.readValue(resourceAsStream, DashScopeModels.class);
        Set<Map<String, String>> resultSet = new HashSet<>();
        for (DashScopeModel model : models.getDashScope()) {
            Map<String, String> modelMap = new HashMap<>();
            modelMap.put(MODEL, model.getName());
            modelMap.put(DESC, model.getDescription());
            resultSet.add(modelMap);
        }
        return resultSet;
    }

    public static List<Model> getModels() {
        return Arrays.stream(ModelEnums.values()).map(a ->
                new Model(a.getName(), a.getDescription())
        ).toList();
    }

    public record Model(String model, String desc) {
    }
}
