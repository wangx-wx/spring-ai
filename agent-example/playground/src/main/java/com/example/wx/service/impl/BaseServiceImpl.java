package com.example.wx.service.impl;

import com.example.wx.exceptions.AppException;
import com.example.wx.service.BaseService;
import com.example.wx.utils.ModelsUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/7/24 21:25
 */
@Service
public class BaseServiceImpl implements BaseService {
    public Set<Map<String, String>> getDashScope() {
        Set<Map<String, String>> resultSet;
        try {
            resultSet = ModelsUtils.getDashScopeModels();
        } catch (IOException e) {
            throw new AppException("Get DashScope Model failed, " + e.getMessage());
        }
        return resultSet;
    }

    public List<ModelsUtils.Model> getModels() {
        return ModelsUtils.getModels();
    }
}
