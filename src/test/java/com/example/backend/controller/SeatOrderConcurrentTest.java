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
 * 选座和订单并发测试类
 * 
 * <p>测试范围：</p>
 * <ul>
 *   <li>并发选座测试（座位冲突检测）</li>
 *   <li>并发创建订单测试</li>
 *   <li>并发支付测试</li>
 *   <li>并发选座+创建订单+支付测试</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("选座和订单并发测试")
public class SeatOrderConcurrentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api";
    private static final String TEST_EMAIL = "diy4869@gmail.com";
    private static final String TEST_PASSWORD = "123456";
    private static final int THREAD_COUNT = 20; // 并发线程数
    private static final int REQUEST_COUNT = 5; // 每个线程的请求数
    private static final int STRESS_TEST_THREAD_COUNT = 1000; // 压力测试并发线程数（1000人同时选座）
    
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
     * 并发选座测试 - 测试座位冲突检测
     */
    @Test
    @DisplayName("并发选座测试 - 座位冲突检测")
    public void testConcurrentSelectSeat() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT * REQUEST_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < REQUEST_COUNT; j++) {
                    try {
                        Map<String, Object> requestBody = new HashMap<>();
                        requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
                        requestBody.put("theaterHallId", TEST_THEATER_HALL_ID);
                        
                        List<Map<String, Object>> seatPositions = new ArrayList<>();
                        Map<String, Object> seat = new HashMap<>();
                        // 所有线程尝试选择同一个座位（测试冲突检测）
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

                        String responseContent = result.getResponse().getContentAsString();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                        
                        if (responseMap.get("code") != null) {
                            Integer code = (Integer) responseMap.get("code");
                            if (code == 200) {
                                successCount.incrementAndGet();
                            } else {
                                conflictCount.incrementAndGet();
                                errors.offer("Thread " + threadId + " Request " + j + ": " + responseMap.get("msg"));
                            }
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        errors.offer("Thread " + threadId + " Request " + j + " Exception: " + e.getMessage());
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

        System.out.println("\n========== 并发选座测试结果 ==========");
        System.out.println("总请求数: " + totalRequests);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("冲突请求数: " + conflictCount.get());
        System.out.println("错误请求数: " + errorCount.get());
        System.out.println("总耗时: " + totalTime + "ms (" + (totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", (double) totalRequests / (totalTime / 1000.0)) + " 请求/秒");
        
        // 理论上只有一个请求应该成功，其他都应该检测到冲突
        System.out.println("预期成功数: 1");
        System.out.println("实际成功数: " + successCount.get());
        System.out.println("冲突检测率: " + String.format("%.2f", (double) conflictCount.get() / totalRequests * 100) + "%");
        
        if (errors.size() > 0) {
            System.out.println("\n错误列表（前10条）:");
            errors.stream().limit(10).forEach(System.out::println);
        }
        System.out.println("====================================\n");
    }

    /**
     * 压力测试 - 1000人同时选座
     * 测试系统在高并发场景下的表现
     */
    @Test
    @DisplayName("压力测试 - 1000人同时选座")
    public void testStressSelectSeat1000Users() throws Exception {
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

        System.out.println("\n========== 开始压力测试 - 1000人同时选座 ==========");
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
                    // 所有1000个用户尝试选择同一个座位（测试冲突检测）
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

                    String responseContent = result.getResponse().getContentAsString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                    
                    if (responseMap.get("code") != null) {
                        Integer code = (Integer) responseMap.get("code");
                        if (code == 200) {
                            successCount.incrementAndGet();
                            System.out.println("用户 " + userId + " 选座成功");
                        } else {
                            conflictCount.incrementAndGet();
                            String errorMsg = (String) responseMap.get("msg");
                            if (errorMsg != null && errorMsg.contains("座位冲突")) {
                                // 正常的冲突检测，不记录为错误
                            } else {
                                errors.offer("用户 " + userId + ": " + errorMsg);
                            }
                        }
                    } else {
                        errorCount.incrementAndGet();
                        errors.offer("用户 " + userId + ": 响应格式错误");
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    errors.offer("用户 " + userId + " Exception: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有请求完成，最多等待10分钟
        boolean completed = latch.await(10, TimeUnit.MINUTES);
        executor.shutdown();

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

        System.out.println("\n========== 压力测试结果 - 1000人同时选座 ==========");
        System.out.println("测试完成状态: " + (completed ? "✅ 正常完成" : "⚠️ 超时"));
        System.out.println("并发用户数: " + STRESS_TEST_THREAD_COUNT);
        System.out.println("成功选座数: " + successCount.get());
        System.out.println("冲突检测数: " + conflictCount.get());
        System.out.println("错误请求数: " + errorCount.get());
        System.out.println("冲突检测率: " + String.format("%.2f", conflictDetectionRate) + "%");
        System.out.println("错误率: " + String.format("%.2f", errorRate) + "%");
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
        
        if (errors.size() > 0) {
            System.out.println("\n错误列表（前20条）:");
            errors.stream().limit(20).forEach(System.out::println);
            if (errors.size() > 20) {
                System.out.println("... 还有 " + (errors.size() - 20) + " 条错误未显示");
            }
        }
        
        System.out.println("\n测试结论:");
        if (successCount.get() == 1 && conflictCount.get() >= STRESS_TEST_THREAD_COUNT - 1 - 10) {
            System.out.println("✅ 压力测试通过：座位冲突检测正常，数据一致性良好");
        } else if (successCount.get() > 1) {
            System.out.println("⚠️ 警告：出现多个用户同时选座成功，可能存在数据不一致问题");
        } else if (successCount.get() == 0) {
            System.out.println("⚠️ 警告：没有任何用户成功选座，可能系统存在问题");
        }
        
        System.out.println("结束时间: " + new java.util.Date());
        System.out.println("==================================================\n");
    }

    /**
     * 并发创建订单测试
     */
    @Test
    @DisplayName("并发创建订单测试")
    public void testConcurrentCreateOrder() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

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
                        requestBody.put("movieShowTimeId", TEST_MOVIE_SHOW_TIME_ID);
                        
                        List<Map<String, Object>> seats = new ArrayList<>();
                        Map<String, Object> seat = new HashMap<>();
                        seat.put("x", 10 + j); // 使用不同的座位
                        seat.put("y", 10 + j);
                        seat.put("seatId", 100 + j);
                        seat.put("movieTicketTypeId", 1);
                        seats.add(seat);
                        requestBody.put("seat", seats);

                        var result = mockMvc.perform(post(BASE_URL + "/movieOrder/create")
                                        .header("token", token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(requestBody)))
                                .andReturn();

                        long requestEnd = System.currentTimeMillis();
                        responseTimes.offer(requestEnd - requestStart);

                        String responseContent = result.getResponse().getContentAsString();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                        
                        if (responseMap.get("code") != null && responseMap.get("code").equals(200)) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
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

        long[] times = responseTimes.stream().mapToLong(Long::longValue).sorted().toArray();
        long avgResponseTime = times.length > 0 ? (long) responseTimes.stream().mapToLong(Long::longValue).average().orElse(0) : 0;

        System.out.println("\n========== 并发创建订单测试结果 ==========");
        System.out.println("总请求数: " + totalRequests);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("失败请求数: " + errorCount.get());
        System.out.println("错误率: " + String.format("%.2f", (double) errorCount.get() / totalRequests * 100) + "%");
        System.out.println("总耗时: " + totalTime + "ms (" + (totalTime / 1000.0) + "s)");
        System.out.println("吞吐量: " + String.format("%.2f", (double) totalRequests / (totalTime / 1000.0)) + " 请求/秒");
        System.out.println("平均响应时间: " + avgResponseTime + "ms");
        System.out.println("========================================\n");
    }

    /**
     * 并发支付测试
     */
    @Test
    @DisplayName("并发支付测试")
    public void testConcurrentPayOrder() throws Exception {
        String token = loginAndGetToken();
        if (token == null || token.isEmpty()) {
            System.out.println("登录失败，跳过测试");
            return;
        }

        // 注意：这个测试需要先有多个已创建但未支付的订单
        // 实际测试中需要先创建订单
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        Integer orderId = 1; // 使用同一个订单ID，测试重复支付检测

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("orderId", orderId);
                    requestBody.put("payId", 1);

                    var result = mockMvc.perform(post(BASE_URL + "/movieOrder/pay")
                                    .header("token", token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestBody)))
                            .andReturn();

                    String responseContent = result.getResponse().getContentAsString();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = objectMapper.readValue(responseContent, Map.class);
                    
                    if (responseMap.get("code") != null) {
                        Integer code = (Integer) responseMap.get("code");
                        if (code == 200) {
                            successCount.incrementAndGet();
                        } else {
                            duplicateCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.MINUTES);
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("\n========== 并发支付测试结果 ==========");
        System.out.println("总请求数: " + THREAD_COUNT);
        System.out.println("成功支付数: " + successCount.get());
        System.out.println("重复支付检测数: " + duplicateCount.get());
        System.out.println("错误数: " + errorCount.get());
        System.out.println("总耗时: " + totalTime + "ms (" + (totalTime / 1000.0) + "s)");
        System.out.println("预期成功数: 1（只有第一次支付应该成功）");
        System.out.println("实际成功数: " + successCount.get());
        System.out.println("====================================\n");
    }
}
