package com.example.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * SQL 性能拦截器
 * 记录所有 SQL 语句的执行耗时
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SqlPerformanceInterceptor implements Interceptor {

    /**
     * 慢查询阈值（毫秒），超过此时间的查询会记录为 WARN 级别
     */
    private static final long SLOW_QUERY_THRESHOLD = 1000; // 1秒

    /**
     * 格式化耗时：超过1000毫秒显示为秒，否则显示毫秒
     */
    private static String formatDuration(long durationMs) {
        if (durationMs >= 1000) {
            double seconds = durationMs / 1000.0;
            return String.format("%.2fs", seconds);
        }
        return durationMs + "ms";
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行 SQL
            Object result = invocation.proceed();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 获取 SQL 信息
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
            String sqlId = mappedStatement.getId();
            
            // 记录执行耗时
            logSqlPerformance(sqlId, sql, duration);
            
            return result;
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录执行失败的情况
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            String sqlId = mappedStatement.getId();
            RequestContextHolder.RequestInfo requestInfo = RequestContextHolder.getRequestInfo();
            String displayInfo = buildApiInfo(requestInfo, sqlId);
            String apiInfo = requestInfo != null ? 
                String.format("接口: [%s %s]", requestInfo.getMethod(), requestInfo.getUri()) : 
                String.format("来源: [%s]", displayInfo);
            // 格式化耗时
            String durationStr = formatDuration(duration);
            log.error("███████████████████████████████████████████████████████████████████");
            log.error("接口：{}", apiInfo);
            log.error("【SQL执行失败】耗时: {} | {}", durationStr, apiInfo);
            log.error("SQL ID: {}", sqlId);
            log.error("错误信息: {}", e.getMessage());
            log.error("███████████████████████████████████████████████████████████████████");
            
            throw e;
        }
    }

    /**
     * 记录 SQL 执行性能
     */
    private void logSqlPerformance(String sqlId, String sql, long duration) {
        // 获取当前请求信息
        RequestContextHolder.RequestInfo requestInfo = RequestContextHolder.getRequestInfo();
        
        // 构建接口信息：如果有 HTTP 请求上下文，显示接口路径；否则显示 SQL ID（可能是定时任务等）
        String apiInfo = buildApiInfo(requestInfo, sqlId);
        
        // 格式化耗时
        String durationStr = formatDuration(duration);
        
        // 如果执行时间超过阈值，记录为 WARN 级别
        if (duration >= SLOW_QUERY_THRESHOLD) {
            // 慢查询使用更显眼的格式，使用多次 log 调用实现真正的多行显示
            String displayInfo = requestInfo != null ? 
                String.format("接口: [%s %s]", requestInfo.getMethod(), requestInfo.getUri()) : 
                String.format("来源: [%s]", apiInfo);
            log.warn("═══════════════════════════════════════════════════════════════════════");
            log.warn("【慢查询警告】耗时: {} | {}", durationStr, displayInfo);
            log.warn("SQL ID: {}", sqlId);
            log.warn("SQL: {}", truncateSql(sql));
            log.warn("═══════════════════════════════════════════════════════════════════════");
        } else {
            // 正常查询使用简洁格式
            log.info("【SQL执行】{} | 耗时: {} | ID: {} | SQL: {}", 
                    apiInfo, durationStr, sqlId, truncateSql(sql));
        }
    }

    /**
     * 构建接口信息
     * 如果有 HTTP 请求上下文，显示接口路径；否则从 SQL ID 提取信息（可能是定时任务等）
     */
    private String buildApiInfo(RequestContextHolder.RequestInfo requestInfo, String sqlId) {
        if (requestInfo != null) {
            return String.format("[%s %s]", requestInfo.getMethod(), requestInfo.getUri());
        }
        // 没有请求上下文时，从 SQL ID 提取信息（例如：com.example.backend.mapper.MovieMapper.getMovieShowTime）
        if (sqlId != null && sqlId.contains(".")) {
            // 提取 Mapper 类名和方法名
            String[] parts = sqlId.split("\\.");
            if (parts.length >= 2) {
                String mapperName = parts[parts.length - 2]; // Mapper 类名
                String methodName = parts[parts.length - 1]; // 方法名
                // 判断是否为定时任务相关的
                if (sqlId.contains("Scheduled") || sqlId.contains("scheduled")) {
                    return String.format("[定时任务] %s.%s", mapperName, methodName);
                }
                return String.format("[系统] %s.%s", mapperName, methodName);
            }
        }
        return "[系统任务]";
    }

    /**
     * 截断过长的 SQL 语句（超过 500 字符）
     */
    private String truncateSql(String sql) {
        if (sql == null) {
            return "null";
        }
        int maxLength = 500;
        if (sql.length() > maxLength) {
            return sql.substring(0, maxLength) + "...";
        }
        return sql;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件读取属性
    }
}
