package com.example.wx.entiy.dashscope;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 18:07
 */
public class DashScopeModels implements Serializable {

    @Serial
    private static final long serialVersionUID = 2123534567887673L;

    private List<DashScopeModel> dashScope;

    public List<DashScopeModel> getDashScope() {
        return dashScope;
    }

    public void setDashScope(List<DashScopeModel> dashScope) {
        this.dashScope = dashScope;
    }

}