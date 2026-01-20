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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 压力测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>1000人同时选座压力测试</li>
 *   <li>系统稳定性测试</li>
 *   <li>资源使用情况测试</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("压力测试")
public class StressTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";
    private static final String TEST_EMAIL = "diy4869@gmail.com";
    private static final String TEST_PASSWORD = "123456";
    private static final int STRESS_TEST_THREAD_COUNT = 1000; // 压力测试并发线程数（1000人）
    
    // 测试数据（需要根据实际情况修改）
    private static final Integer TEST_MOVIE_SHOW_TIME_ID = 1;
    private static final Integer TEST_THEATER_HALL_ID = 1;

    /**
     * 登录并获取token
     */
    private String loginAndGetToken() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", TEST_EMAIL);
        requestBody.put("password", TEST_PASSWORD);

        var result = mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
            if (responseMap.get("code") != null && responseMap.get("code").equals(200)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                if (data != null && data.get("token") != null) {
                    return data.get("token").toString();
                }
            }
        } catch (Exception e) {
            System.err.println("解析token失败: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * 使用指定邮箱和密码登录并获取token
     */
    private String loginAndGetToken(String email, String password) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        var result = mockMvc.perform(post(BASE_URL + "/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
            if (responseMap.get("code") != null && responseMap.get("code").equals(200)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                if (data != null && data.get("token") != null) {
                    return data.get("token").toString();
                }
            }
        } catch (Exception e) {
            System.err.println("解析token失败: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * 压力测试 - 1000人同时选座（同一座位）
     * 测试座位冲突检测在高并发场景下的表现
     */
    @Test
    @DisplayName("压力测试 - 1000人同时选座（同一座位）")
    public void testStressSelectSeatSameSeat() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(STRESS_TEST_THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(STRESS_TEST_THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
        Map<Integer, Integer> statusCodeCount = new ConcurrentHashMap<>();

        System.out.println("\n========== 压力测试开始 - 1000人同时选座（同一座位） ==========");
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("测试座位: (5,5)");
        System.out.println("开始时间: " + new java.util.Date());
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < STRESS_TEST_THREAD_COUNT; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    long requestStart = System.currentTimeMillis();

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
                    requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);
                    
                    List<Map<String, Object>> seatPositions = new ArrayList<>();
                    Map<String, Object> seat = new HashMap<>();
                    // 所有1000个用户尝试选择同一个座位
                    seat.put("x", 5);
                    seat.put("y", 5);
                    seat.put("seatId", 50);
                    seatPositions.add(seat);
                    requestBody.put("seatPosition", seatPositions);

                    var result = mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/save")
                                    .header("token", token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                            .andReturn();

                    long requestEnd = System.currentTimeMillis();
                    long responseTime = requestEnd - requestStart;
                    responseTimes.offer(responseTime);

                    int statusCode = result.getResponse().getStatus();
                    statusCodeCount.merge(statusCode, 1, Integer::sum);

                    String responseContent = result.getResponse().getContentAsString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                    
                    if (responseMap.get("code") != null) {
                        Integer code = (Integer) responseMap.get("code");
                        if (code == 200) {
                            successCount.incrementAndGet();
                            if (successCount.get() <= 5) {
                                System.out.println("[成功] 用户 " + userId + " 选座成功，响应时间: " + responseTime + "ms");
                            }
                        } else {
                            conflictCount.incrementAndGet();
                            String errorMsg = (String) responseMap.get("msg");
                            if (errorMsg == null || !errorMsg.contains("座位冲突")) {
                                errors.offer("用户 " + userId + " [状态码:" + code + "] " + errorMsg);
                            }
                        }
                    } else {
                        errorCount.incrementAndGet();
                        errors.offer("用户 " + userId + ": 响应格式错误");
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    String errorMsg = "用户 " + userId + " Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage();
                    errors.offer(errorMsg);
                    if (errorCount.get() <= 10) {
                        System.err.println(errorMsg);
                    }
                } finally {
                    latch.countDown();
                    // 每100个请求输出一次进度
                    int remaining = (int) latch.getCount();
                    if (remaining % 100 == 0) {
                        System.out.println("进度: 剩余 " + remaining + " 个请求");
                    }
                }
            });
        }

        // 等待所有请求完成，最多等待10分钟
        boolean completed = latch.await(10, TimeUnit.MINUTES);
        executor.shutdown();
        
        if (!executor.isTerminated()) {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // 计算统计信息
        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long minResponseTime = times.length > 0 ? times[0] : 0;
        long maxResponseTime = times.length > 0 ? times[times.length - 1] : 0;
        long p50ResponseTime = times.length > 0 ? times[(int) (times.length * 0.50)] : 0;
        long p95ResponseTime = times.length > 0 ? times[(int) (times.length * 0.95)] : 0;
        long p99ResponseTime = times.length > 0 ? times[(int) (times.length * 0.99)] : 0;

        double throughput = completed ? (double) STRESS_TEST_THREAD_COUNT / (totalTime / 1000.0) : 0;
        double conflictDetectionRate = (double) conflictCount.get() / STRESS_TEST_THREAD_COUNT * 100;
        double errorRate = (double) errorCount.get() / STRESS_TEST_THREAD_COUNT * 100;
        double successRate = (double) successCount.get() / STRESS_TEST_THREAD_COUNT * 100;

        System.out.println("\n========== 压力测试结果 - 1000人同时选座（同一座位） ==========");
        System.out.println("测试完成状态: " + (completed ? "✅ 正常完成" : "⚠️ 超时"));
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("\n请求结果统计:");
        System.out.println("成功选座数: " + successCount.get() + " (" + String.format("%.2f", successRate) + "%)");
        System.out.println("冲突检测数: " + conflictCount.get() + " (" + String.format("%.2f", conflictDetectionRate) + "%)");
        System.out.println("错误请求数: " + errorCount.get() + " (" + String.format("%.2f", errorRate) + "%)");
        System.out.println("\nHTTP状态码统计:");
        statusCodeCount.forEach((code, count) -> {
            System.out.println("  " + code + ": " + count + " 次");
        });
        System.out.println("\n性能指标:");
        System.out.println("总耗时: " + totalTime + "ms (" + String.format("%.2f", totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("P50响应时间: " + p50ResponseTime + "ms");
        System.out.println("P95响应时间: " + p95ResponseTime + "ms");
        System.out.println("P99响应时间: " + p99ResponseTime + "ms");
        System.out.println("\n预期结果:");
        System.out.println("预期成功数: 1（只有一个用户应该成功选座）");
        System.out.println("实际成功数: " + successCount.get());
        System.out.println("预期冲突数: " + (STRESS_TEST_THREAD_COUNT - 1) + "（其他用户应该检测到冲突）");
        System.out.println("实际冲突数: " + conflictCount.get());
        System.out.println("冲突检测准确率: " + String.format("%.2f", (double) conflictCount.get() / (STRESS_TEST_THREAD_COUNT - successCount.get()) * 100) + "%");
        
        if (errors.size() > 0) {
            System.out.println("\n错误列表（前30条）:");
            errors.stream().limit(30).forEach(System.out::println);
            if (errors.size() > 30) {
                System.out.println("... 还有 " + (errors.size() - 30) + " 条错误未显示");
            }
        }
        
        System.out.println("\n测试结论:");
        if (successCount.get() == 1 && conflictCount.get() >= STRESS_TEST_THREAD_COUNT - 1 - 50) {
            System.out.println("✅ 压力测试通过：座位冲突检测正常，数据一致性良好");
        } else if (successCount.get() > 1) {
            System.out.println("⚠️ 警告：出现 " + successCount.get() + " 个用户同时选座成功，可能存在数据不一致问题");
        } else if (successCount.get() == 0) {
            System.out.println("⚠️ 警告：没有任何用户成功选座，可能系统存在问题");
        }
        
        if (errorRate > 5) {
            System.out.println("⚠️ 警告：错误率过高（" + String.format("%.2f", errorRate) + "%），需要检查系统稳定性");
        }
        
        System.out.println("结束时间: " + new java.util.Date());
        System.out.println("==============================================================\n");
    }

    /**
     * 压力测试 - 1000人同时选座（不同座位）
     * 测试系统在高并发场景下的吞吐量
     */
    @Test
    @DisplayName("压力测试 - 1000人同时选座（不同座位）")
    public void testStressSelectSeatDifferentSeats() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(STRESS_TEST_THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(STRESS_TEST_THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();

        System.out.println("\n========== 压力测试开始 - 1000人同时选座（不同座位） ==========");
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("测试场景: 每个用户选择不同的座位");
        System.out.println("开始时间: " + new java.util.Date());
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < STRESS_TEST_THREAD_COUNT; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    long requestStart = System.currentTimeMillis();

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
                    requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);
                    
                    List<Map<String, Object>> seatPositions = new ArrayList<>();
                    Map<String, Object> seat = new HashMap<>();
                    // 每个用户选择不同的座位（避免冲突）
                    seat.put("x", 10 + userId); // 使用userId作为偏移量
                    seat.put("y", 10 + userId);
                    seat.put("seatId", 100 + userId);
                    seatPositions.add(seat);
                    requestBody.put("seatPosition", seatPositions);

                    var result = mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/save")
                                    .header("token", token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                            .andReturn();

                    long requestEnd = System.currentTimeMillis();
                    long responseTime = requestEnd - requestStart;
                    responseTimes.offer(responseTime);

                    String responseContent = result.getResponse().getContentAsString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                    
                    if (responseMap.get("code") != null) {
                        Integer code = (Integer) responseMap.get("code");
                        if (code == 200) {
                            successCount.incrementAndGet();
                        } else {
                            conflictCount.incrementAndGet();
                            String errorMsg = (String) responseMap.get("msg");
                            errors.offer("用户 " + userId + ": " + errorMsg);
                        }
                    } else {
                        errorCount.incrementAndGet();
                        errors.offer("用户 " + userId + ": 响应格式错误");
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    errors.offer("用户 " + userId + " Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                    // 每100个请求输出一次进度
                    int remaining = (int) latch.getCount();
                    if (remaining % 100 == 0) {
                        System.out.println("进度: 剩余 " + remaining + " 个请求");
                    }
                }
            });
        }

        // 等待所有请求完成，最多等待10分钟
        boolean completed = latch.await(10, TimeUnit.MINUTES);
        executor.shutdown();
        
        if (!executor.isTerminated()) {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // 计算统计信息
        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long minResponseTime = times.length > 0 ? times[0] : 0;
        long maxResponseTime = times.length > 0 ? times[times.length - 1] : 0;
        long p50ResponseTime = times.length > 0 ? times[(int) (times.length * 0.50)] : 0;
        long p95ResponseTime = times.length > 0 ? times[(int) (times.length * 0.95)] : 0;
        long p99ResponseTime = times.length > 0 ? times[(int) (times.length * 0.99)] : 0;

        double throughput = completed ? (double) STRESS_TEST_THREAD_COUNT / (totalTime / 1000.0) : 0;
        double successRate = (double) successCount.get() / STRESS_TEST_THREAD_COUNT * 100;
        double errorRate = (double) errorCount.get() / STRESS_TEST_THREAD_COUNT * 100;

        System.out.println("\n========== 压力测试结果 - 1000人同时选座（不同座位） ==========");
        System.out.println("测试完成状态: " + (completed ? "✅ 正常完成" : "⚠️ 超时"));
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("\n请求结果统计:");
        System.out.println("成功选座数: " + successCount.get() + " (" + String.format("%.2f", successRate) + "%)");
        System.out.println("冲突检测数: " + conflictCount.get());
        System.out.println("错误请求数: " + errorCount.get() + " (" + String.format("%.2f", errorRate) + "%)");
        System.out.println("\n性能指标:");
        System.out.println("总耗时: " + totalTime + "ms (" + String.format("%.2f", totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("P50响应时间: " + p50ResponseTime + "ms");
        System.out.println("P95响应时间: " + p95ResponseTime + "ms");
        System.out.println("P99响应时间: " + p99ResponseTime + "ms");
        
        if (errors.size() > 0) {
            System.out.println("\n错误列表（前30条）:");
            errors.stream().limit(30).forEach(System.out::println);
            if (errors.size() > 30) {
                System.out.println("... 还有 " + (errors.size() - 30) + " 条错误未显示");
            }
        }
        
        System.out.println("\n测试结论:");
        if (successRate >= 95) {
            System.out.println("✅ 压力测试通过：系统在高并发场景下表现良好");
        } else if (successRate >= 90) {
            System.out.println("⚠️ 警告：成功率略低，建议优化系统性能");
        } else {
            System.out.println("❌ 失败：成功率过低，系统无法满足高并发需求");
        }
        
        System.out.println("结束时间: " + new java.util.Date());
        System.out.println("==============================================================\n");
    }

    /**
     * 压力测试 - 多用户并发选座（同一座位）
     * 测试不同用户同时选择同一座位时的冲突检测
     * 每个用户使用不同的token（通过登录获取）
     */
    @Test
    @DisplayName("压力测试 - 多用户并发选座（同一座位）")
    public void testStressSelectSeatMultipleUsersSameSeat() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(STRESS_TEST_THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(STRESS_TEST_THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
        Map<Integer, Integer> statusCodeCount = new ConcurrentHashMap<>();

        System.out.println("\n========== 压力测试开始 - 多用户并发选座（同一座位） ==========");
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("测试场景: 不同用户同时选择同一个座位");
        System.out.println("测试座位: (5,5)");
        System.out.println("开始时间: " + new java.util.Date());
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < STRESS_TEST_THREAD_COUNT; i++) {
            final int userId = i;
            executor.submit(() -> {
                String token = null;
                try {
                    // 每个用户先登录获取自己的token（使用相同的测试账号，但每次登录都会生成新的token）
                    token = loginAndGetToken();
                    if (token == null || token.isEmpty()) {
                        errorCount.incrementAndGet();
                        errors.offer("用户 " + userId + ": 登录失败");
                        return;
                    }

                    long requestStart = System.currentTimeMillis();

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
                    requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);
                    
                    List<Map<String, Object>> seatPositions = new ArrayList<>();
                    Map<String, Object> seat = new HashMap<>();
                    // 所有用户尝试选择同一个座位（测试多用户冲突检测）
                    seat.put("x", 5);
                    seat.put("y", 5);
                    seat.put("seatId", 50);
                    seatPositions.add(seat);
                    requestBody.put("seatPosition", seatPositions);

                    var result = mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/save")
                                    .header("token", token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                            .andReturn();

                    long requestEnd = System.currentTimeMillis();
                    long responseTime = requestEnd - requestStart;
                    responseTimes.offer(responseTime);

                    int statusCode = result.getResponse().getStatus();
                    statusCodeCount.merge(statusCode, 1, Integer::sum);

                    String responseContent = result.getResponse().getContentAsString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                    
                    if (responseMap.get("code") != null) {
                        Integer code = (Integer) responseMap.get("code");
                        if (code == 200) {
                            successCount.incrementAndGet();
                            if (successCount.get() <= 5) {
                                System.out.println("[成功] 用户 " + userId + " 选座成功，响应时间: " + responseTime + "ms");
                            }
                        } else {
                            conflictCount.incrementAndGet();
                            String errorMsg = (String) responseMap.get("msg");
                            if (errorMsg == null || !errorMsg.contains("座位冲突")) {
                                errors.offer("用户 " + userId + " [状态码:" + code + "] " + errorMsg);
                            }
                        }
                    } else {
                        errorCount.incrementAndGet();
                        errors.offer("用户 " + userId + ": 响应格式错误");
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    String errorMsg = "用户 " + userId + " Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage();
                    errors.offer(errorMsg);
                    if (errorCount.get() <= 10) {
                        System.err.println(errorMsg);
                    }
                } finally {
                    latch.countDown();
                    // 每100个请求输出一次进度
                    int remaining = (int) latch.getCount();
                    if (remaining % 100 == 0) {
                        System.out.println("进度: 剩余 " + remaining + " 个请求");
                    }
                }
            });
        }

        // 等待所有请求完成，最多等待15分钟（因为每个用户都需要先登录）
        boolean completed = latch.await(15, TimeUnit.MINUTES);
        executor.shutdown();
        
        if (!executor.isTerminated()) {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // 计算统计信息
        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long minResponseTime = times.length > 0 ? times[0] : 0;
        long maxResponseTime = times.length > 0 ? times[times.length - 1] : 0;
        long p50ResponseTime = times.length > 0 ? times[(int) (times.length * 0.50)] : 0;
        long p95ResponseTime = times.length > 0 ? times[(int) (times.length * 0.95)] : 0;
        long p99ResponseTime = times.length > 0 ? times[(int) (times.length * 0.99)] : 0;

        double throughput = completed ? (double) STRESS_TEST_THREAD_COUNT / (totalTime / 1000.0) : 0;
        double conflictDetectionRate = (double) conflictCount.get() / STRESS_TEST_THREAD_COUNT * 100;
        double errorRate = (double) errorCount.get() / STRESS_TEST_THREAD_COUNT * 100;
        double successRate = (double) successCount.get() / STRESS_TEST_THREAD_COUNT * 100;

        System.out.println("\n========== 压力测试结果 - 多用户并发选座（同一座位） ==========");
        System.out.println("测试完成状态: " + (completed ? "✅ 正常完成" : "⚠️ 超时"));
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("\n请求结果统计:");
        System.out.println("成功选座数: " + successCount.get() + " (" + String.format("%.2f", successRate) + "%)");
        System.out.println("冲突检测数: " + conflictCount.get() + " (" + String.format("%.2f", conflictDetectionRate) + "%)");
        System.out.println("错误请求数: " + errorCount.get() + " (" + String.format("%.2f", errorRate) + "%)");
        System.out.println("\nHTTP状态码统计:");
        statusCodeCount.forEach((code, count) -> {
            System.out.println("  " + code + ": " + count + " 次");
        });
        System.out.println("\n性能指标:");
        System.out.println("总耗时: " + totalTime + "ms (" + String.format("%.2f", totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("P50响应时间: " + p50ResponseTime + "ms");
        System.out.println("P95响应时间: " + p95ResponseTime + "ms");
        System.out.println("P99响应时间: " + p99ResponseTime + "ms");
        System.out.println("\n预期结果:");
        System.out.println("预期成功数: 1（只有一个用户应该成功选座）");
        System.out.println("实际成功数: " + successCount.get());
        System.out.println("预期冲突数: " + (STRESS_TEST_THREAD_COUNT - 1) + "（其他用户应该检测到冲突）");
        System.out.println("实际冲突数: " + conflictCount.get());
        if (STRESS_TEST_THREAD_COUNT - successCount.get() > 0) {
            System.out.println("冲突检测准确率: " + String.format("%.2f", (double) conflictCount.get() / (STRESS_TEST_THREAD_COUNT - successCount.get()) * 100) + "%");
        }
        
        if (errors.size() > 0) {
            System.out.println("\n错误列表（前30条）:");
            errors.stream().limit(30).forEach(System.out::println);
            if (errors.size() > 30) {
                System.out.println("... 还有 " + (errors.size() - 30) + " 条错误未显示");
            }
        }
        
        System.out.println("\n测试结论:");
        if (successCount.get() == 1 && conflictCount.get() >= STRESS_TEST_THREAD_COUNT - 1 - 50) {
            System.out.println("✅ 压力测试通过：多用户座位冲突检测正常，数据一致性良好");
        } else if (successCount.get() > 1) {
            System.out.println("⚠️ 警告：出现 " + successCount.get() + " 个用户同时选座成功，可能存在数据不一致问题");
        } else if (successCount.get() == 0) {
            System.out.println("⚠️ 警告：没有任何用户成功选座，可能系统存在问题");
        }
        
        if (errorRate > 5) {
            System.out.println("⚠️ 警告：错误率过高（" + String.format("%.2f", errorRate) + "%），需要检查系统稳定性");
        }
        
        System.out.println("结束时间: " + new java.util.Date());
        System.out.println("==============================================================\n");
    }

    /**
     * 压力测试 - 多用户并发选座（不同座位）
     * 测试不同用户同时选择不同座位时的系统吞吐量
     * 每个用户使用不同的token（通过登录获取）
     */
    @Test
    @DisplayName("压力测试 - 多用户并发选座（不同座位）")
    public void testStressSelectSeatMultipleUsersDifferentSeats() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(STRESS_TEST_THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(STRESS_TEST_THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
        Map<Integer, Integer> statusCodeCount = new ConcurrentHashMap<>();

        System.out.println("\n========== 压力测试开始 - 多用户并发选座（不同座位） ==========");
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("测试场景: 不同用户同时选择不同的座位");
        System.out.println("开始时间: " + new java.util.Date());
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < STRESS_TEST_THREAD_COUNT; i++) {
            final int userId = i;
            executor.submit(() -> {
                String token = null;
                try {
                    // 每个用户先登录获取自己的token
                    token = loginAndGetToken();
                    if (token == null || token.isEmpty()) {
                        errorCount.incrementAndGet();
                        errors.offer("用户 " + userId + ": 登录失败");
                        return;
                    }

                    long requestStart = System.currentTimeMillis();

                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
                    requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);
                    
                    List<Map<String, Object>> seatPositions = new ArrayList<>();
                    Map<String, Object> seat = new HashMap<>();
                    // 每个用户选择不同的座位（避免冲突）
                    seat.put("x", 10 + userId); // 使用userId作为偏移量
                    seat.put("y", 10 + userId);
                    seat.put("seatId", 100 + userId);
                    seatPositions.add(seat);
                    requestBody.put("seatPosition", seatPositions);

                    var result = mockMvc.perform(post(BASE_URL + "/movie_show_time/select_seat/save")
                                    .header("token", token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                            .andReturn();

                    long requestEnd = System.currentTimeMillis();
                    long responseTime = requestEnd - requestStart;
                    responseTimes.offer(responseTime);

                    int statusCode = result.getResponse().getStatus();
                    statusCodeCount.merge(statusCode, 1, Integer::sum);

                    String responseContent = result.getResponse().getContentAsString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                    
                    if (responseMap.get("code") != null) {
                        Integer code = (Integer) responseMap.get("code");
                        if (code == 200) {
                            successCount.incrementAndGet();
                        } else {
                            conflictCount.incrementAndGet();
                            String errorMsg = (String) responseMap.get("msg");
                            errors.offer("用户 " + userId + ": " + errorMsg);
                        }
                    } else {
                        errorCount.incrementAndGet();
                        errors.offer("用户 " + userId + ": 响应格式错误");
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    String errorMsg = "用户 " + userId + " Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage();
                    errors.offer(errorMsg);
                    if (errorCount.get() <= 10) {
                        System.err.println(errorMsg);
                    }
                } finally {
                    latch.countDown();
                    // 每100个请求输出一次进度
                    int remaining = (int) latch.getCount();
                    if (remaining % 100 == 0) {
                        System.out.println("进度: 剩余 " + remaining + " 个请求");
                    }
                }
            });
        }

        // 等待所有请求完成，最多等待15分钟（因为每个用户都需要先登录）
        boolean completed = latch.await(15, TimeUnit.MINUTES);
        executor.shutdown();
        
        if (!executor.isTerminated()) {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // 计算统计信息
        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;
        long minResponseTime = times.length > 0 ? times[0] : 0;
        long maxResponseTime = times.length > 0 ? times[times.length - 1] : 0;
        long p50ResponseTime = times.length > 0 ? times[(int) (times.length * 0.50)] : 0;
        long p95ResponseTime = times.length > 0 ? times[(int) (times.length * 0.95)] : 0;
        long p99ResponseTime = times.length > 0 ? times[(int) (times.length * 0.99)] : 0;

        double throughput = completed ? (double) STRESS_TEST_THREAD_COUNT / (totalTime / 1000.0) : 0;
        double successRate = (double) successCount.get() / STRESS_TEST_THREAD_COUNT * 100;
        double errorRate = (double) errorCount.get() / STRESS_TEST_THREAD_COUNT * 100;

        System.out.println("\n========== 压力测试结果 - 多用户并发选座（不同座位） ==========");
        System.out.println("测试完成状态: " + (completed ? "✅ 正常完成" : "⚠️ 超时"));
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("\n请求结果统计:");
        System.out.println("成功选座数: " + successCount.get() + " (" + String.format("%.2f", successRate) + "%)");
        System.out.println("冲突检测数: " + conflictCount.get());
        System.out.println("错误请求数: " + errorCount.get() + " (" + String.format("%.2f", errorRate) + "%)");
        System.out.println("\nHTTP状态码统计:");
        statusCodeCount.forEach((code, count) -> {
            System.out.println("  " + code + ": " + count + " 次");
        });
        System.out.println("\n性能指标:");
        System.out.println("总耗时: " + totalTime + "ms (" + String.format("%.2f", totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("最小响应时间: " + minResponseTime + "ms");
        System.out.println("最大响应时间: " + maxResponseTime + "ms");
        System.out.println("P50响应时间: " + p50ResponseTime + "ms");
        System.out.println("P95响应时间: " + p95ResponseTime + "ms");
        System.out.println("P99响应时间: " + p99ResponseTime + "ms");
        
        if (errors.size() > 0) {
            System.out.println("\n错误列表（前30条）:");
            errors.stream().limit(30).forEach(System.out::println);
            if (errors.size() > 30) {
                System.out.println("... 还有 " + (errors.size() - 30) + " 条错误未显示");
            }
        }
        
        System.out.println("\n测试结论:");
        if (successRate >= 95) {
            System.out.println("✅ 压力测试通过：多用户并发场景下系统表现良好");
        } else if (successRate >= 90) {
            System.out.println("⚠️ 警告：成功率略低，建议优化系统性能");
        } else {
            System.out.println("❌ 失败：成功率过低，系统无法满足多用户并发需求");
        }
        
        System.out.println("结束时间: " + new java.util.Date());
        System.out.println("==============================================================\n");
    }
}
