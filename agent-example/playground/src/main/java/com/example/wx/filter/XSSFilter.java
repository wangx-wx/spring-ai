package com.example.wx.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 17:32
 */
public class XSSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Setting up CPS allows for inline styling and cross-domain fetching of images
        httpResponse.setHeader(
                "Content-Security-Policy",
                "default-src 'self'; font-src 'self' data:; style-src 'self' 'unsafe-inline'; img-src 'self' https://mdn.alipayobjects.com blob: data:;");
        chain.doFilter(request, response);
    }

}