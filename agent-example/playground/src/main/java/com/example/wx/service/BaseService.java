package com.example.wx.service;

import com.example.wx.utils.ModelsUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/7/24 21:24
 */
public interface BaseService {
    Set<Map<String, String>> getDashScope();
    List<ModelsUtils.Model> getModels();
}
