package com.example.wx.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 15:42
 */
public class AppException extends RuntimeException {

    private final static Logger logger = LoggerFactory.getLogger(AppException.class);

    public AppException(String msg) {
        super(msg);
        logger.error("Spring AI Playground app exception: {}", msg);
    }

}
