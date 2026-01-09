package com.example.wx.domain;

import java.util.List;

/**
 * @author wangx
 * @description
 * @create 2026/1/9 23:48
 */
public class ChatResult {

    // text option end error
    private String type;

    private String content;

    private List<Option> options;

    public static class Option {
        private String code;
        private String value;
    }
}
