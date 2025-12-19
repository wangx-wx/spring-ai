package com.example.wx.domain;

import com.example.wx.domain.tool.BaseToolRequest;
import lombok.Data;

@Data
public class ToolResult {

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ERROR = -1;

    private String name;
    private int code;
    private String message;
    private BaseToolRequest request;
    private Object data;

    public boolean isSuccess() {
        return code == CODE_SUCCESS;
    }

    public boolean isError() {
        return code == CODE_ERROR;
    }

    public static ToolResult success(String name, Object data) {
       return success(name, null, data);
    }

    public static ToolResult success(String name, BaseToolRequest request, Object data) {
        ToolResult result = new ToolResult();
        result.setName(name);
        result.setCode(CODE_SUCCESS);
        result.setRequest(request);
        result.setData(data);
        return result;
    }

    public static ToolResult error(String name, String message) {
        ToolResult result = new ToolResult();
        result.setName(name);
        result.setCode(CODE_ERROR);
        result.setMessage(message);
        return result;
    }
}