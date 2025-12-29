package com.example.wx.entiy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 16:14
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_time", nullable = false)
    private String requestTime;

    @Column(name = "request_ip", nullable = false)
    private String requestIp;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    @Column(name = "max_request_count", nullable = false)
    private int maxRequestCount = Integer.MAX_VALUE;

    @Column(name = "request_uri", nullable = false)
    private String requestUri;

    public User() {
    }

    public User(
            String requestTime,
            String requestIp,
            int requestCount,
            String requestUri
    ) {
        this.requestTime = requestTime;
        this.requestIp = requestIp;
        this.requestCount = requestCount;
        this.requestUri = requestUri;
    }

    public static class Builder {
        private String requestTime;
        private String requestIp;
        private int requestCount;
        private String requestUri;

        public Builder setRequestTime(String requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public Builder setRequestIp(String requestIp) {
            this.requestIp = requestIp;
            return this;
        }

        public Builder setRequestCount(int requestCount) {
            this.requestCount = requestCount;
            return this;
        }

        public Builder setRequestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }

        public User build() {
            return new User(requestTime, requestIp, requestCount, requestUri);
        }
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getMaxRequestCount() {
        return maxRequestCount;
    }

    public void setMaxRequestCount(int maxRequestCount) {
        this.maxRequestCount = maxRequestCount;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
}
