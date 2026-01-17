package com.example.backend.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 请求上下文持有者
 * 用于在线程中存储 HTTP 请求信息，以便在 SQL 拦截器等地方使用
 */
public class RequestContextHolder {
    
    private static final ThreadLocal<RequestInfo> REQUEST_INFO = new ThreadLocal<>();

    @Getter
    @Setter
    public static class RequestInfo {
        private String method;      // HTTP 方法：GET, POST, DELETE 等
        private String uri;         // 请求 URI
        private String queryString; // 查询字符串
        private String apiPath;     // API 路径（去除查询参数）
        private Long startTime;     // 请求开始时间（毫秒时间戳）

        public String getFullPath() {
            if (queryString != null && !queryString.isEmpty()) {
                return uri + "?" + queryString;
            }
            return uri;
        }
    }

    /**
     * 设置当前请求信息
     */
    public static void setRequestInfo(RequestInfo requestInfo) {
        REQUEST_INFO.set(requestInfo);
    }

    /**
     * 获取当前请求信息
     */
    public static RequestInfo getRequestInfo() {
        return REQUEST_INFO.get();
    }

    /**
     * 清除当前请求信息（重要：避免内存泄漏）
     */
    public static void clear() {
        REQUEST_INFO.remove();
    }
}
