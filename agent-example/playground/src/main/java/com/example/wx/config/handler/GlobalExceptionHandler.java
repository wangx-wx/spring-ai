package com.example.wx.config.handler;

import com.example.wx.entiy.result.Result;
import com.example.wx.exceptions.AIException;
import com.example.wx.exceptions.AppException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 15:43
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        return Result.failed("Invalid params: " + ex.getMessage());
    }

    @ExceptionHandler(AIException.class)
    public Result<?> handleSAAAIExceptions(
            MethodArgumentNotValidException ex
    ) {
        return Result.failed("Spring AI Alibaba Exception: " + ex.getMessage());
    }

    @ExceptionHandler(AppException.class)
    public Result<?> handleSAAAppExceptions(
            MethodArgumentNotValidException ex
    ) {
        return Result.failed("Spring AI Alibaba Playground Exception: " + ex.getMessage());
    }

}
