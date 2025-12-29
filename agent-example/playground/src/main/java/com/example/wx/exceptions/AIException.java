package com.example.wx.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 15:42
 */
public class AIException extends RuntimeException {

    private final static Logger logger = LoggerFactory.getLogger(AIException.class);

    public AIException(String msg) {
        super(msg);
        logger.error("Spring AI Playground app exception: {}", msg);
    }

}
