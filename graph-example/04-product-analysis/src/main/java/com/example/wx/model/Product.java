package com.example.wx.model;

import java.util.List;

/**
 * @author wangx
 * @description
 * @create 2025/10/8 18:10
 */
public record Product(String slogan, String material, List<String> colors, String season) {
}

