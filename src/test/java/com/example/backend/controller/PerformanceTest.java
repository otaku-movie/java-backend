package com.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 压力测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>并发请求测试</li>
 *   <li>响应时间测试</li>
 *   <li>吞吐量测试</li>
 *   <li>错误率测试</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("压力测试")
public class PerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";
    private static final int THREAD_COUNT = 50; // 并发线程数
    private static final int REQUEST_COUNT = 100; // 每个线程的请求数

    /**
     * 压力测试 - 电影详情接口
     */
    @Test
    @DisplayName("压力测试 - 电影详情接口")
    public void testMovieDetailPerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT * REQUEST_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < REQUEST_COUNT; j++) {
                    try {
                        long requestStart = System.currentTimeMillis();
                        
                        mockMvc.perform(get(BASE_URL + "/movie/detail")
                                        .param("id", "1"))
                                .andReturn();
                        
                        long requestEnd = System.currentTimeMillis();
                        long responseTime = requestEnd - requestStart;
                        responseTimes.offer(responseTime);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("请求失败: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES); // 等待最多5分钟
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalRequests = THREAD_COUNT * REQUEST_COUNT;

        // 计算统计信息
        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long minResponseTime = times.length > 0 ? times[0] : 0;
        long maxResponseTime = times.length > 0 ? times[times.length - 1] : 0;
        long p95ResponseTime = times.length > 0 ? times[(int) (times.length * 0.95)] : 0;
        long p99ResponseTime = times.length > 0 ? times[(int) (times.length * 0.99)] : 0;

        double throughput = (double) totalRequests / (totalTime / 1000.0); // 每秒请求数
        double errorRate = (double) errorCount.get() / totalRequests * 100; // 错误率

        System.out.println("\n========== 压力测试结果 - 电影详情接口 ==========");
        System.out.println("总请求数: " + totalRequests);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("失败请求数: " + errorCount.get());
        System.out.println("错误率: " + String.format("%.2f", errorRate) + "%");
        System.out.println("总耗时: " + totalTime + "ms (" + (totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("P95响应时间: " + p95ResponseTime + "ms");
        System.out.println("P99响应时间: " + p99ResponseTime + "ms");
        System.out.println("==============================================\n");
    }

    /**
     * 压力测试 - 电影版本列表接口
     */
    @Test
    @DisplayName("压力测试 - 电影版本列表接口")
    public void testMovieVersionListPerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT * REQUEST_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < REQUEST_COUNT; j++) {
                    try {
                        long requestStart = System.currentTimeMillis();
                        
                        mockMvc.perform(get(BASE_URL + "/movie/version/list")
                                        .param("movieId", "34860"))
                                .andReturn();
                        
                        long requestEnd = System.currentTimeMillis();
                        long responseTime = requestEnd - requestStart;
                        responseTimes.offer(responseTime);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("请求失败: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES);
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalRequests = THREAD_COUNT * REQUEST_COUNT;

        // 计算统计信息
        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long minResponseTime = times.length > 0 ? times[0] : 0;
        long maxResponseTime = times.length > 0 ? times[times.length - 1] : 0;
        long p95ResponseTime = times.length > 0 ? times[(int) (times.length * 0.95)] : 0;
        long p99ResponseTime = times.length > 0 ? times[(int) (times.length * 0.99)] : 0;

        double throughput = (double) totalRequests / (totalTime / 1000.0);
        double errorRate = (double) errorCount.get() / totalRequests * 100;

        System.out.println("\n========== 压力测试结果 - 电影版本列表接口 ==========");
        System.out.println("总请求数: " + totalRequests);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("失败请求数: " + errorCount.get());
        System.out.println("错误率: " + String.format("%.2f", errorRate) + "%");
        System.out.println("总耗时: " + totalTime + "ms (" + (totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("P95响应时间: " + p95ResponseTime + "ms");
        System.out.println("P99响应时间: " + p99ResponseTime + "ms");
        System.out.println("==================================================\n");
    }

    /**
     * 压力测试 - 电影场次接口
     */
    @Test
    @DisplayName("压力测试 - 电影场次接口")
    public void testMovieShowTimePerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT * REQUEST_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < REQUEST_COUNT; j++) {
                    try {
                        long requestStart = System.currentTimeMillis();
                        
                        Map<String, Object> requestBody = new HashMap<>();
                        requestBody.put("movieId", 1);
                        requestBody.put("page", 1);
                        requestBody.put("pageSize", 10);
                        
                        mockMvc.perform(post("/api/app/movie/showTime")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(requestBody)))
                                .andReturn();
                        
                        long requestEnd = System.currentTimeMillis();
                        long responseTime = requestEnd - requestStart;
                        responseTimes.offer(responseTime);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("请求失败: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES);
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalRequests = THREAD_COUNT * REQUEST_COUNT;

        // 计算统计信息
        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long minResponseTime = times.length > 0 ? times[0] : 0;
        long maxResponseTime = times.length > 0 ? times[times.length - 1] : 0;
        long p95ResponseTime = times.length > 0 ? times[(int) (times.length * 0.95)] : 0;
        long p99ResponseTime = times.length > 0 ? times[(int) (times.length * 0.99)] : 0;

        double throughput = (double) totalRequests / (totalTime / 1000.0);
        double errorRate = (double) errorCount.get() / totalRequests * 100;

        System.out.println("\n========== 压力测试结果 - 电影场次接口 ==========");
        System.out.println("总请求数: " + totalRequests);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("失败请求数: " + errorCount.get());
        System.out.println("错误率: " + String.format("%.2f", errorRate) + "%");
        System.out.println("总耗时: " + totalTime + "ms (" + (totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("P95响应时间: " + p95ResponseTime + "ms");
        System.out.println("P99响应时间: " + p99ResponseTime + "ms");
        System.out.println("============================================\n");
    }
}
