package com.example.wx.entiy.result;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 15:44
 */
public enum ResultCode {

    SUCCESS(10000, "success"),
    FAILED(100001, "failed");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
