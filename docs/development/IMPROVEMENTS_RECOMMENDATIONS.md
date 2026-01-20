# 选座、订单、支付系统改善建议

## 文档概述

本文档基于对选座、订单、支付系统的全面测试，提供了详细的改善建议和实施方案。

**文档日期**: 2025-01-17  
**测试范围**: 选座、订单、支付相关接口  
**测试类型**: 功能测试、并发测试、压力测试  

---

## 1. 总体改善建议

### 1.1 优先级分类

| 优先级 | 说明 | 数量 |
|--------|------|------|
| 🔴 高 | 必须立即实施，影响系统稳定性和数据一致性 | 8 |
| 🟡 中 | 建议尽快实施，提升性能和用户体验 | 6 |
| 🟢 低 | 可选实施，长期优化建议 | 4 |

---

## 2. 选座系统改善建议

### 2.1 🔴 高优先级：座位冲突检测优化

#### 问题描述

**当前实现**:
- 在代码中查询已选座位，然后循环检查冲突
- 在高并发场景下可能出现竞态条件
- 吞吐量相对较低（~6.67 请求/秒）

**影响**:
- 可能出现座位重复选择的情况
- 用户体验差（响应慢）
- 系统负载高

#### 解决方案

**方案1: 数据库唯一索引（推荐）**

**实施步骤**:
1. 在数据库层面添加唯一索引
2. 使用数据库约束来防止重复选择

```sql
-- 添加唯一索引（排除已删除的记录）
CREATE UNIQUE INDEX idx_select_seat_unique 
ON select_seat (movie_show_time_id, theater_hall_id, x, y) 
WHERE deleted = 0;

-- 添加部分唯一索引（PostgreSQL支持）
-- 确保同一场次同一影厅的同一座位只能被选择一次
```

**优点**:
- 数据库层面保证唯一性，避免竞态条件
- 实现简单，无需额外组件
- 性能好，数据库会自动处理冲突

**缺点**:
- 需要数据库支持部分唯一索引（PostgreSQL支持，MySQL 8.0+支持）
- 冲突时会抛出数据库异常，需要处理异常

**实施难度**: ⭐⭐ 简单  
**预期效果**: 冲突检测率100%，吞吐量提升50%+

---

**方案2: Redis分布式锁**

**实施步骤**:
1. 引入Redis依赖
2. 实现分布式锁机制
3. 在选座前获取锁，选座后释放锁

```java
@Service
public class SeatSelectionService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public RestBean<Object> saveSelectSeat(SaveSelectSeatQuery query) {
        List<String> lockKeys = query.getSeatPosition().stream()
            .map(seat -> "seat:lock:" + query.getMovieShowTimeId() + 
                         ":" + query.getTheaterHallId() + 
                         ":" + seat.getX() + ":" + seat.getY())
            .collect(Collectors.toList());
        
        String lockValue = UUID.randomUUID().toString();
        List<Boolean> locksAcquired = new ArrayList<>();
        
        try {
            // 尝试获取所有座位的锁
            for (String lockKey : lockKeys) {
                Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockValue, 15, TimeUnit.SECONDS);
                locksAcquired.add(acquired);
            }
            
            // 如果所有锁都获取成功，执行选座操作
            if (locksAcquired.stream().allMatch(Boolean::booleanValue)) {
                // 检查座位是否可选
                // 保存选座信息
                return RestBean.success(null, ...);
            } else {
                // 释放已获取的锁
                releaseLocks(lockKeys, lockValue);
                return RestBean.error("座位正在被其他用户选择，请稍后再试");
            }
        } catch (Exception e) {
            // 发生异常时释放锁
            releaseLocks(lockKeys, lockValue);
            throw e;
        }
    }
    
    private void releaseLocks(List<String> lockKeys, String lockValue) {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";
        
        for (String lockKey : lockKeys) {
            redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(lockKey),
                lockValue
            );
        }
    }
}
```

**优点**:
- 性能好，避免数据库锁竞争
- 支持分布式环境
- 可以实现更细粒度的控制（如锁超时时间）

**缺点**:
- 需要引入Redis
- 需要考虑锁超时和释放问题
- 需要处理Redis故障的情况

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 吞吐量提升50%+，冲突检测率100%

---

**推荐方案**: 组合使用方案1和方案2

**实施步骤**:
1. 首先实施方案1（数据库唯一索引），保证数据一致性
2. 然后实施方案2（Redis分布式锁），提升性能
3. 数据库唯一索引作为最后防线，Redis锁作为性能优化

---

### 2.2 🔴 高优先级：选座超时机制

#### 问题描述

**当前问题**:
- 用户选座后没有明确的超时释放机制
- 可能出现座位被长期占用但不创建订单的情况
- 影响其他用户的选座体验

#### 解决方案

**方案: 选座锁定机制**

**实施步骤**:
1. 增加座位锁定状态（`locked`）
2. 设置锁定超时时间（15分钟）
3. 使用定时任务或延迟队列自动释放

```java
// 选座时设置锁定状态和超时时间
SelectSeat selectSeat = new SelectSeat();
selectSeat.setSelectSeatState(SeatState.locked.getCode()); // 锁定状态
selectSeat.setLockExpireTime(LocalDateTime.now().plusMinutes(15)); // 15分钟后过期

// 定时任务释放超时的锁定座位
@Scheduled(fixedRate = 60000) // 每分钟执行一次
public void releaseExpiredSeats() {
    LocalDateTime now = LocalDateTime.now();
    UpdateWrapper<SelectSeat> updateWrapper = new UpdateWrapper<>();
    updateWrapper.eq("select_seat_state", SeatState.locked.getCode())
                 .le("lock_expire_time", now)
                 .set("select_seat_state", SeatState.available.getCode());
    
    selectSeatMapper.update(null, updateWrapper);
}
```

**实施难度**: ⭐⭐ 简单  
**预期效果**: 座位利用率提升30%+

---

### 2.3 🟡 中优先级：座位状态缓存

#### 问题描述

**当前问题**:
- 每次选座都需要查询数据库
- 高并发场景下数据库压力大

#### 解决方案

**方案: Redis缓存座位状态**

```java
@Service
public class SeatCacheService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String SEAT_STATUS_KEY = "seat:status:%d:%d:%d:%d";
    
    public boolean isSeatAvailable(Integer movieShowTimeId, Integer theaterHallId, Integer x, Integer y) {
        String key = String.format(SEAT_STATUS_KEY, movieShowTimeId, theaterHallId, x, y);
        Object status = redisTemplate.opsForValue().get(key);
        
        if (status == null) {
            // 从数据库查询
            status = querySeatFromDatabase(movieShowTimeId, theaterHallId, x, y);
            // 缓存5分钟
            redisTemplate.opsForValue().set(key, status, 5, TimeUnit.MINUTES);
        }
        
        return "available".equals(status);
    }
    
    public void updateSeatStatus(Integer movieShowTimeId, Integer theaterHallId, Integer x, Integer y, String status) {
        String key = String.format(SEAT_STATUS_KEY, movieShowTimeId, theaterHallId, x, y);
        redisTemplate.opsForValue().set(key, status, 5, TimeUnit.MINUTES);
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 响应时间减少30%，吞吐量提升20%

---

## 3. 订单系统改善建议

### 3.1 🔴 高优先级：订单创建性能优化

#### 问题描述

**当前问题**:
- 订单创建涉及多次数据库查询
- 计算订单总金额需要多次查询
- 吞吐量相对较低（~3.33 请求/秒）

#### 解决方案

**方案1: 批量查询优化**

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public MovieOrder createOrder(MovieOrderSaveQuery query) throws Exception {
    // 批量查询所有需要的数据
    Integer userId = StpUtil.getLoginIdAsInt();
    Integer movieShowTimeId = query.getMovieShowTimeId();
    
    // 1. 批量查询座位信息
    List<Integer> seatIds = query.getSeat().stream()
        .map(SeatGroup::getSeatId)
        .collect(Collectors.toList());
    List<Seat> seats = seatMapper.selectBatchIds(seatIds);
    Map<Integer, Seat> seatMap = seats.stream()
        .collect(Collectors.toMap(Seat::getId, seat -> seat));
    
    // 2. 批量查询票种价格
    List<Integer> ticketTypeIds = query.getSeat().stream()
        .map(SeatGroup::getMovieTicketTypeId)
        .distinct()
        .collect(Collectors.toList());
    List<MovieTicketType> ticketTypes = movieTicketTypeMapper.selectBatchIds(ticketTypeIds);
    Map<Integer, MovieTicketType> ticketTypeMap = ticketTypes.stream()
        .collect(Collectors.toMap(MovieTicketType::getId, type -> type));
    
    // 3. 一次性查询用户选座信息（使用JOIN优化）
    UserSelectSeat userSelectSeat = movieShowTimeMapper.userSelectSeatWithSpec(
        userId, movieShowTimeId, SeatState.selected.getCode());
    
    // 4. 计算订单总金额（在内存中计算，不查询数据库）
    BigDecimal totalAmount = calculateTotalAmount(query, seatMap, ticketTypeMap, userSelectSeat);
    
    // 5. 创建订单
    MovieOrder order = new MovieOrder();
    order.setMovieShowTimeId(movieShowTimeId);
    order.setUserId(userId);
    order.setTotalAmount(totalAmount);
    order.setOrderState(OrderState.order_created.getCode());
    movieOrderMapper.insert(order);
    
    // 6. 批量更新选座状态
    List<Integer> selectSeatIds = query.getSeat().stream()
        .map(seat -> getSelectSeatId(userSelectSeat, seat))
        .collect(Collectors.toList());
    selectSeatMapper.updateBatchById(selectSeatIds, SeatState.ordered.getCode());
    
    return order;
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 响应时间减少30%，吞吐量提升50%

---

**方案2: 异步处理非关键操作**

```java
@Service
public class OrderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public MovieOrder createOrder(MovieOrderSaveQuery query) throws Exception {
        // 同步处理关键操作
        MovieOrder order = createOrderSync(query);
        
        // 异步处理非关键操作
        rabbitTemplate.convertAndSend("order.created", order);
        
        return order;
    }
    
    @RabbitListener(queues = "order.created")
    public void handleOrderCreated(MovieOrder order) {
        // 发送通知
        sendNotification(order);
        
        // 更新统计数据
        updateStatistics(order);
        
        // 记录日志
        logOrderCreated(order);
    }
}
```

**实施难度**: ⭐⭐⭐⭐ 较难  
**预期效果**: 响应时间减少40%

---

### 3.2 🔴 高优先级：订单超时处理优化

#### 问题描述

**当前问题**:
- 依赖定时任务扫描超时订单
- 可能存在延迟处理的情况

#### 解决方案

**方案: 延迟队列自动处理**

```java
@Service
public class OrderTimeoutService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void scheduleOrderTimeout(Integer orderId, int timeoutMinutes) {
        // 使用Redis延迟队列
        String key = "order:timeout:" + orderId;
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(timeoutMinutes);
        redisTemplate.opsForValue().set(key, orderId, timeoutMinutes, TimeUnit.MINUTES);
        
        // 或使用RabbitMQ延迟队列
        rabbitTemplate.convertAndSend(
            "order.timeout.exchange",
            "order.timeout",
            orderId,
            message -> {
                message.getMessageProperties().setDelay(timeoutMinutes * 60 * 1000);
                return message;
            }
        );
    }
    
    @RabbitListener(queues = "order.timeout")
    public void handleOrderTimeout(Integer orderId) {
        movieOrderService.updateCancelOrTimeoutOrder(orderId, "timeout");
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 订单超时处理更及时，准确性100%

---

### 3.3 🟡 中优先级：订单状态机

#### 问题描述

**当前问题**:
- 订单状态流转逻辑分散在多个地方
- 状态转换规则不明确
- 容易出现状态不一致的情况

#### 解决方案

**方案: 状态机模式**

```java
public enum OrderState {
    CREATED(1, "订单已创建"),
    PAID(2, "订单已支付"),
    CANCELED(4, "订单已取消"),
    TIMEOUT(5, "订单已超时");
    
    private final int code;
    private final String description;
    
    // 定义状态转换规则
    private static final Map<OrderState, Set<OrderState>> TRANSITIONS = Map.of(
        CREATED, Set.of(PAID, CANCELED, TIMEOUT),
        PAID, Set.of(), // 终态
        CANCELED, Set.of(), // 终态
        TIMEOUT, Set.of() // 终态
    );
    
    public boolean canTransitionTo(OrderState target) {
        return TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
    }
}

@Service
public class OrderStateMachine {
    public void transition(MovieOrder order, OrderState targetState) {
        OrderState currentState = OrderState.fromCode(order.getOrderState());
        
        if (!currentState.canTransitionTo(targetState)) {
            throw new IllegalStateException(
                String.format("订单状态不能从 %s 转换到 %s", currentState, targetState));
        }
        
        order.setOrderState(targetState.getCode());
        movieOrderMapper.updateById(order);
        
        // 记录状态变更历史
        logStateChange(order.getId(), currentState, targetState);
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 状态管理更清晰，减少状态不一致问题

---

## 4. 支付系统改善建议

### 4.1 🔴 高优先级：支付幂等性保证

#### 问题描述

**当前问题**:
- 虽然通过订单状态防止重复支付，但缺少支付幂等性机制
- 在高并发场景下可能出现问题
- 无法处理网络重试等情况

#### 解决方案

**方案1: 支付流水号**

```java
@Service
public class PaymentService {
    public RestBean<Null> pay(MovieOrderPayQuery query) {
        // 生成支付流水号
        String paymentSerialNumber = generatePaymentSerialNumber(query.getOrderId());
        
        // 检查支付流水号是否已存在
        PaymentRecord existingPayment = paymentMapper.selectBySerialNumber(paymentSerialNumber);
        if (existingPayment != null) {
            // 幂等性：返回已存在的支付结果
            if (existingPayment.getStatus() == PaymentStatus.SUCCESS) {
                return RestBean.success(null, "支付已完成");
            } else if (existingPayment.getStatus() == PaymentStatus.FAILED) {
                return RestBean.error("支付失败");
            }
        }
        
        // 创建支付记录
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(query.getOrderId());
        paymentRecord.setPayId(query.getPayId());
        paymentRecord.setSerialNumber(paymentSerialNumber);
        paymentRecord.setStatus(PaymentStatus.PENDING);
        paymentMapper.insert(paymentRecord);
        
        try {
            // 执行支付
            paymentMethodService.pay(query.getOrderId(), query.getPayId(), paymentSerialNumber);
            
            // 更新支付记录
            paymentRecord.setStatus(PaymentStatus.SUCCESS);
            paymentMapper.updateById(paymentRecord);
            
            // 更新订单状态
            movieOrderService.updateOrderState(query.getOrderId(), OrderState.order_succeed);
            
            return RestBean.success(null, "支付成功");
        } catch (Exception e) {
            // 更新支付记录为失败
            paymentRecord.setStatus(PaymentStatus.FAILED);
            paymentRecord.setErrorMessage(e.getMessage());
            paymentMapper.updateById(paymentRecord);
            
            return RestBean.error("支付失败: " + e.getMessage());
        }
    }
    
    private String generatePaymentSerialNumber(Integer orderId) {
        return "PAY" + orderId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 支付幂等性100%，支持网络重试

---

**方案2: 乐观锁防止重复支付**

```java
public RestBean<Null> pay(MovieOrderPayQuery query) {
    // 使用乐观锁更新订单状态
    MovieOrder order = movieOrderMapper.selectById(query.getOrderId());
    
    if (order.getOrderState() != OrderState.order_created.getCode()) {
        return RestBean.error("订单状态错误，无法支付");
    }
    
    UpdateWrapper<MovieOrder> updateWrapper = new UpdateWrapper<>();
    updateWrapper.eq("id", query.getOrderId())
                 .eq("order_state", OrderState.order_created.getCode())
                 .eq("version", order.getVersion()) // 乐观锁
                 .set("order_state", OrderState.order_succeed.getCode())
                 .set("version", order.getVersion() + 1);
    
    int updateCount = movieOrderMapper.update(null, updateWrapper);
    
    if (updateCount > 0) {
        // 支付成功
        movieOrderService.pay(query.getOrderId(), query.getPayId());
        return RestBean.success(null, "支付成功");
    } else {
        // 订单状态已被其他请求修改，支付失败
        return RestBean.error("订单状态已变更，无法支付");
    }
}
```

**实施难度**: ⭐⭐ 简单  
**预期效果**: 重复支付检测率100%

---

**推荐方案**: 组合使用方案1和方案2

**实施步骤**:
1. 实施方案2（乐观锁），保证并发场景下的数据一致性
2. 实施方案1（支付流水号），保证支付幂等性和网络重试支持

---

### 4.2 🔴 高优先级：支付安全性增强

#### 问题描述

**当前问题**:
- 支付接口缺少签名验证
- 缺少金额验证
- 缺少频率限制

#### 解决方案

**方案: 支付安全验证**

```java
@Service
public class PaymentSecurityService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 1. 签名验证
    public boolean verifySignature(MovieOrderPayQuery query, String signature) {
        String data = query.getOrderId() + ":" + query.getPayId();
        String expectedSignature = HmacSHA256(data, SECRET_KEY);
        return expectedSignature.equals(signature);
    }
    
    // 2. 金额验证
    public boolean verifyAmount(Integer orderId, BigDecimal amount) {
        MovieOrder order = movieOrderMapper.selectById(orderId);
        return order.getTotalAmount().compareTo(amount) == 0;
    }
    
    // 3. 频率限制
    public boolean checkRateLimit(Integer userId) {
        String key = "payment:rate:limit:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        
        return count <= 10; // 每分钟最多10次支付请求
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 支付安全性显著提升，防止恶意请求

---

### 4.3 🟡 中优先级：支付异步处理

#### 问题描述

**当前问题**:
- 支付接口同步处理，响应时间较长
- 第三方支付网关可能响应慢

#### 解决方案

**方案: 支付异步处理**

```java
@Service
public class AsyncPaymentService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public RestBean<Null> pay(MovieOrderPayQuery query) {
        // 创建支付记录，状态为处理中
        PaymentRecord paymentRecord = createPaymentRecord(query);
        
        // 异步处理支付
        rabbitTemplate.convertAndSend("payment.process", paymentRecord);
        
        // 立即返回，告诉用户支付正在处理中
        return RestBean.success(null, "支付正在处理中，请稍候");
    }
    
    @RabbitListener(queues = "payment.process")
    public void processPayment(PaymentRecord paymentRecord) {
        try {
            // 调用支付网关
            PaymentResult result = paymentGateway.pay(
                paymentRecord.getOrderId(),
                paymentRecord.getPayId(),
                paymentRecord.getAmount()
            );
            
            // 更新支付记录
            paymentRecord.setStatus(result.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
            paymentMapper.updateById(paymentRecord);
            
            // 更新订单状态
            if (result.isSuccess()) {
                movieOrderService.updateOrderState(
                    paymentRecord.getOrderId(), 
                    OrderState.order_succeed
                );
            }
            
            // 发送通知
            sendPaymentNotification(paymentRecord, result);
        } catch (Exception e) {
            // 处理失败
            paymentRecord.setStatus(PaymentStatus.FAILED);
            paymentRecord.setErrorMessage(e.getMessage());
            paymentMapper.updateById(paymentRecord);
        }
    }
    
    // 支付状态查询接口
    public RestBean<PaymentStatus> getPaymentStatus(String paymentSerialNumber) {
        PaymentRecord payment = paymentMapper.selectBySerialNumber(paymentSerialNumber);
        return RestBean.success(payment.getStatus(), "查询成功");
    }
}
```

**实施难度**: ⭐⭐⭐⭐ 较难  
**预期效果**: 响应时间减少60%，用户体验提升

---

## 5. 数据库优化建议

### 5.1 🔴 高优先级：索引优化

#### 问题描述

**当前问题**:
- 缺少关键索引，查询性能差
- 并发场景下容易出现全表扫描

#### 解决方案

```sql
-- 选座表索引
CREATE INDEX idx_select_seat_show_time 
ON select_seat (movie_show_time_id, theater_hall_id, deleted);

CREATE UNIQUE INDEX idx_select_seat_unique 
ON select_seat (movie_show_time_id, theater_hall_id, x, y) 
WHERE deleted = 0;

CREATE INDEX idx_select_seat_user 
ON select_seat (user_id, movie_show_time_id, select_seat_state, deleted);

-- 订单表索引
CREATE INDEX idx_order_user_state 
ON movie_order (user_id, order_state, deleted);

CREATE INDEX idx_order_create_time 
ON movie_order (create_time, order_state);

CREATE INDEX idx_order_show_time 
ON movie_order (movie_show_time_id, order_state, deleted);

-- 支付表索引
CREATE UNIQUE INDEX idx_payment_serial 
ON payment (serial_number);

CREATE INDEX idx_payment_order 
ON payment (order_id, payment_state);

CREATE INDEX idx_payment_create_time 
ON payment (create_time, payment_state);
```

**实施难度**: ⭐⭐ 简单  
**预期效果**: 查询性能提升50%+

---

### 5.2 🟡 中优先级：查询优化

#### 解决方案

1. **批量查询优化**:
   ```java
   // 使用批量查询代替循环查询
   List<Seat> seats = seatMapper.selectBatchIds(seatIds);
   ```

2. **JOIN查询优化**:
   ```sql
   -- 使用JOIN代替多次查询
   SELECT s.*, st.name as seat_type_name
   FROM seat s
   LEFT JOIN seat_type st ON s.seat_type_id = st.id
   WHERE s.id IN (1, 2, 3);
   ```

3. **分页优化**:
   ```sql
   -- 使用游标分页代替偏移量分页
   SELECT * FROM movie_order
   WHERE id > ? AND user_id = ?
   ORDER BY id
   LIMIT 10;
   ```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 响应时间减少20-30%

---

## 6. 缓存优化建议

### 6.1 🔴 高优先级：热点数据缓存

#### 解决方案

```java
@Service
public class CacheService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存座位状态
    public void cacheSeatStatus(Integer movieShowTimeId, Integer theaterHallId, 
                                Map<String, String> seatStatus) {
        String key = "seat:status:" + movieShowTimeId + ":" + theaterHallId;
        redisTemplate.opsForHash().putAll(key, seatStatus);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
    }
    
    // 缓存价格信息
    public void cachePrices(Integer cinemaId, Integer movieShowTimeId, 
                           Map<String, BigDecimal> prices) {
        String key = "prices:" + cinemaId + ":" + movieShowTimeId;
        redisTemplate.opsForHash().putAll(key, prices);
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }
    
    // 缓存订单信息（短期缓存）
    public void cacheOrder(Integer orderId, MovieOrder order) {
        String key = "order:" + orderId;
        redisTemplate.opsForValue().set(key, order, 5, TimeUnit.MINUTES);
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 响应时间减少30-40%，吞吐量提升50%+

---

### 6.2 🟡 中优先级：缓存预热

#### 解决方案

```java
@Component
public class CacheWarmupService {
    @PostConstruct
    public void warmupCache() {
        // 预热即将上映的电影场次座位状态
        List<MovieShowTime> upcomingShowTimes = movieShowTimeMapper
            .selectUpcomingShowTimes(LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        
        for (MovieShowTime showTime : upcomingShowTimes) {
            cacheSeatStatusForShowTime(showTime.getId(), showTime.getTheaterHallId());
        }
    }
}
```

**实施难度**: ⭐⭐ 简单  
**预期效果**: 首次查询响应时间减少

---

## 7. 系统架构优化建议

### 7.1 🟡 中优先级：引入消息队列

#### 解决方案

**使用RabbitMQ处理异步操作**:

```java
// 1. 配置RabbitMQ
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable("order.created").build();
    }
    
    @Bean
    public Queue paymentProcessQueue() {
        return QueueBuilder.durable("payment.process").build();
    }
    
    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable("order.timeout")
            .withArgument("x-message-ttl", 15 * 60 * 1000) // 15分钟TTL
            .build();
    }
}

// 2. 发送消息
@Service
public class OrderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public MovieOrder createOrder(MovieOrderSaveQuery query) {
        MovieOrder order = createOrderSync(query);
        
        // 发送消息到队列
        rabbitTemplate.convertAndSend("order.created", order);
        
        return order;
    }
}

// 3. 消费消息
@Component
public class OrderMessageListener {
    @RabbitListener(queues = "order.created")
    public void handleOrderCreated(MovieOrder order) {
        // 发送通知
        notificationService.sendOrderCreatedNotification(order);
        
        // 更新统计数据
        statisticsService.updateOrderStatistics(order);
        
        // 记录日志
        logService.logOrderCreated(order);
    }
}
```

**实施难度**: ⭐⭐⭐⭐ 较难  
**预期效果**: 系统响应时间减少40%，吞吐量提升50%+

---

### 7.2 🟢 低优先级：引入限流机制

#### 解决方案

```java
@Component
public class RateLimitService {
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    public boolean tryAcquire(String key, double permitsPerSecond) {
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(
            key, k -> RateLimiter.create(permitsPerSecond));
        return rateLimiter.tryAcquire();
    }
}

// 使用限流
@RestController
public class SeatSelectionController {
    @Autowired
    private RateLimitService rateLimitService;
    
    @PostMapping("/movie_show_time/select_seat/save")
    public RestBean<Object> saveSelectSeat(@RequestBody SaveSelectSeatQuery query) {
        Integer userId = StpUtil.getLoginIdAsInt();
        
        // 限制每个用户每分钟最多10次选座请求
        if (!rateLimitService.tryAcquire("select_seat:" + userId, 10.0 / 60)) {
            return RestBean.error("请求过于频繁，请稍后再试");
        }
        
        // 处理选座请求
        // ...
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 防止恶意请求，保护系统资源

---

## 8. 监控和日志建议

### 8.1 🟡 中优先级：完善监控指标

#### 解决方案

```java
@Component
public class MetricsService {
    private final MeterRegistry meterRegistry;
    
    // 记录选座请求数
    public void recordSeatSelection() {
        meterRegistry.counter("seat.selection.requests").increment();
    }
    
    // 记录选座冲突数
    public void recordSeatConflict() {
        meterRegistry.counter("seat.selection.conflicts").increment();
    }
    
    // 记录订单创建数
    public void recordOrderCreation() {
        meterRegistry.counter("order.creation.requests").increment();
    }
    
    // 记录支付请求数
    public void recordPaymentRequest() {
        meterRegistry.counter("payment.requests").increment();
    }
    
    // 记录响应时间
    public void recordResponseTime(String endpoint, long duration) {
        meterRegistry.timer("http.request.duration", "endpoint", endpoint)
                     .record(duration, TimeUnit.MILLISECONDS);
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 便于监控系统性能和问题排查

---

### 8.2 🟡 中优先级：完善日志记录

#### 解决方案

```java
@Aspect
@Component
@Slf4j
public class BusinessLogAspect {
    @Around("@annotation(com.example.backend.annotation.BusinessLog)")
    public Object logBusiness(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info("业务操作开始: {}, 参数: {}", methodName, args);
        
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("业务操作成功: {}, 耗时: {}ms, 结果: {}", methodName, duration, result);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("业务操作失败: {}, 耗时: {}ms, 错误: {}", methodName, duration, e.getMessage(), e);
            throw e;
        }
    }
}

// 使用注解
@BusinessLog
public RestBean<Object> saveSelectSeat(@RequestBody SaveSelectSeatQuery query) {
    // ...
}
```

**实施难度**: ⭐⭐ 简单  
**预期效果**: 便于问题排查和审计

---

## 9. 测试建议

### 9.1 🔴 高优先级：增加集成测试

#### 解决方案

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SeatOrderIntegrationTest {
    
    @Test
    @DisplayName("完整流程测试：选座 -> 创建订单 -> 支付")
    public void testCompleteFlow() throws Exception {
        // 1. 登录
        String token = loginAndGetToken();
        
        // 2. 选座
        selectSeat(token, 1, 1);
        
        // 3. 创建订单
        Integer orderId = createOrder(token);
        
        // 4. 支付
        payOrder(token, orderId);
        
        // 5. 验证订单状态
        verifyOrderState(orderId, OrderState.order_succeed);
    }
}
```

**实施难度**: ⭐⭐⭐ 中等  
**预期效果**: 保证系统整体功能正确性

---

### 9.2 🟡 中优先级：增加压力测试

#### 解决方案

```java
@Test
@DisplayName("压力测试：1000并发选座")
public void testSeatSelectionStress() throws Exception {
    int threadCount = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    // ... 压力测试代码
}
```

**实施难度**: ⭐⭐ 简单  
**预期效果**: 发现性能瓶颈

---

## 10. 实施计划

### 10.1 第一阶段（1-2周）

**优先级**: 🔴 高优先级

1. ✅ 数据库唯一索引优化（选座表）
2. ✅ 支付幂等性保证（乐观锁 + 支付流水号）
3. ✅ 订单创建性能优化（批量查询）
4. ✅ 订单超时处理优化（延迟队列）

**预期效果**: 
- 数据一致性显著提升
- 支付安全性提升
- 订单创建性能提升30%

---

### 10.2 第二阶段（2-3周）

**优先级**: 🔴 高优先级 + 🟡 中优先级

1. ✅ Redis分布式锁（选座）
2. ✅ 座位状态缓存
3. ✅ 选座超时机制
4. ✅ 支付安全性增强
5. ✅ 数据库索引优化

**预期效果**:
- 并发性能提升50%+
- 响应时间减少30-40%
- 系统稳定性提升

---

### 10.3 第三阶段（3-4周）

**优先级**: 🟡 中优先级 + 🟢 低优先级

1. ✅ 消息队列引入（异步处理）
2. ✅ 订单状态机
3. ✅ 支付异步处理
4. ✅ 监控和日志完善
5. ✅ 限流机制

**预期效果**:
- 系统响应时间减少40%
- 吞吐量提升50%+
- 可观测性显著提升

---

## 11. 预期收益

### 11.1 性能提升

| 指标 | 当前值 | 预期值 | 提升幅度 |
|------|--------|--------|---------|
| 并发选座吞吐量 | ~6.67 请求/秒 | 50+ 请求/秒 | 650%+ |
| 并发创建订单吞吐量 | ~3.33 请求/秒 | 20+ 请求/秒 | 500%+ |
| 并发支付吞吐量 | ~10.00 请求/秒 | 30+ 请求/秒 | 200%+ |
| 选座响应时间 | ~150ms | <100ms | 33%+ |
| 创建订单响应时间 | ~200ms | <150ms | 25%+ |

### 11.2 数据一致性提升

| 指标 | 当前值 | 预期值 |
|------|--------|--------|
| 座位冲突检测率 | 99% | 100% |
| 重复支付检测率 | 100% | 100% |
| 数据一致性 | 优秀 | 优秀 |

### 11.3 系统稳定性提升

| 指标 | 当前值 | 预期值 |
|------|--------|--------|
| 错误率 | <5% | <1% |
| 系统可用性 | 99% | 99.9% |
| 故障恢复时间 | - | <5分钟 |

---

## 12. 风险评估

### 12.1 实施风险

| 风险 | 影响 | 概率 | 应对措施 |
|------|------|------|---------|
| 数据库索引影响写入性能 | 中等 | 低 | 在非高峰期实施，监控性能 |
| Redis故障影响选座 | 高 | 低 | 实现降级方案，使用数据库锁 |
| 消息队列故障影响异步处理 | 中等 | 低 | 实现同步降级方案 |
| 缓存数据不一致 | 中等 | 中 | 实现缓存失效机制，定期刷新 |

### 12.2 回滚方案

1. **数据库索引回滚**: 直接删除索引
2. **Redis分布式锁回滚**: 切换到数据库锁
3. **消息队列回滚**: 切换到同步处理
4. **缓存回滚**: 禁用缓存，直接查询数据库

---

## 13. 总结

### 13.1 关键改善点

1. **数据一致性**: 通过数据库唯一索引和分布式锁保证
2. **性能优化**: 通过缓存、批量查询、异步处理提升
3. **安全性**: 通过支付幂等性、签名验证、频率限制增强
4. **可观测性**: 通过监控指标和日志记录完善

### 13.2 预期成果

实施所有改善建议后，预期实现：

- ✅ 数据一致性: 100%（座位冲突检测、重复支付检测）
- ✅ 性能提升: 50-650%（根据不同场景）
- ✅ 系统稳定性: 99.9%可用性
- ✅ 用户体验: 响应时间减少30-40%
- ✅ 系统安全性: 显著提升

---

**文档生成时间**: 2025-01-17  
**文档版本**: v1.0  
**审核状态**: 待审核
