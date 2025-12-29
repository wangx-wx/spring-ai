package com.example.wx.controller;

import com.example.wx.entiy.result.Result;
import com.example.wx.service.BaseService;
import com.example.wx.utils.ModelsUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangx
 * @description
 * @create 2025/7/24 21:24
 */
@RestController
@Tag(name = "Base APIs")
@RequestMapping("/api/v1")
public class BaseController {

    private final BaseService baseService;

    public BaseController(BaseService baseService) {
        this.baseService = baseService;
    }

    @GetMapping("/dashscope/getModels")
    public Result<List<ModelsUtils.Model>> getDashScopeModels() {
        List<ModelsUtils.Model> models = baseService.getModels();
        if (models.isEmpty()) {
            return Result.failed("No DashScope models found");
        }
        return Result.success(models);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Spring AI Alibaba Playground is running......");
    }

}
