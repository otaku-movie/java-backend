# 测试说明文档

## 概述

本项目包含完整的 API 测试套件，包括功能测试、接口测试和压力测试。

## 测试类说明

### 1. MovieApiTest - 通用接口测试

测试通用的电影相关接口：

- `testMovieList()` - 测试电影列表接口
- `testMovieDetail()` - 测试电影详情接口
- `testMovieDetailWithInvalidParam()` - 测试电影详情接口参数验证
- `testMovieCharacter()` - 测试电影角色接口
- `testMovieVersionList()` - 测试电影版本列表接口
- `testMovieVersionListWithInvalidParam()` - 测试电影版本列表接口参数验证
- `testMovieStaff()` - 测试电影演职员接口
- `testMovieSpec()` - 测试电影规格接口

### 2. AppMovieApiTest - App 端接口测试

测试 App 端专用接口：

- `testNowShowing()` - 测试正在热映接口
- `testComingSoon()` - 测试即将上映接口
- `testMovieShowTime()` - 测试电影场次接口（基本查询）
- `testMovieShowTimeWithFilters()` - 测试电影场次接口（带筛选条件）
- `testMovieShowTimeWith30HourFormat()` - 测试电影场次接口（30小时制时间格式）
- `testAppMovieStaff()` - 测试 App 端电影演职员接口

### 3. UserLoginTest - 用户登录测试

测试用户相关的接口：

- `testLoginSuccess()` - 测试用户登录接口（成功登录）
- `testLoginWrongPassword()` - 测试用户登录接口（密码错误）
- `testLoginEmailNotFound()` - 测试用户登录接口（邮箱不存在）
- `testLoginMissingEmail()` - 测试用户登录接口（缺少邮箱）
- `testLoginMissingPassword()` - 测试用户登录接口（缺少密码）
- `testLoginInvalidEmail()` - 测试用户登录接口（邮箱格式错误）
- `testUserDetail()` - 测试用户详情接口（需要认证）
- `testUserOrderList()` - 测试用户订单列表接口（需要认证）
- `testLogout()` - 测试用户登出接口
- `testDoubleMd5Encryption()` - 测试密码2次MD5加密

**测试账号**:
- 邮箱: `diy4869@gmail.com`
- 密码: `123456`
- 密码加密方式: 2次MD5加密（`MD5(MD5(password))`）

### 4. SeatSelectionTest - 选座测试

测试选座相关的接口：

- `testSaveSelectSeatSuccess()` - 测试保存选座接口（成功选座）
- `testSaveSelectSeatConflict()` - 测试保存选座接口（座位冲突）
- `testCancelSelectSeat()` - 测试取消选座接口
- `testUserSelectSeatList()` - 测试用户选座列表接口
- `testSelectSeatList()` - 测试选座列表接口
- `testSaveSelectSeatMissingParams()` - 测试参数验证

### 5. OrderTest - 订单测试

测试订单相关的接口：

- `testCreateOrder()` - 测试创建订单接口
- `testOrderDetail()` - 测试订单详情接口
- `testCancelOrder()` - 测试取消订单接口
- `testTimeoutOrder()` - 测试订单超时接口
- `testMyTickets()` - 测试我的票据接口

### 6. PaymentTest - 支付测试

测试支付相关的接口：

- `testPayOrderSuccess()` - 测试支付订单接口（成功支付）
- `testPayOrderWrongState()` - 测试支付订单接口（订单状态错误）
- `testPayOrderDuplicate()` - 测试支付订单接口（重复支付）
- `testPayOrderMissingParams()` - 测试参数验证

### 7. SeatOrderConcurrentTest - 并发测试

测试选座和订单的并发场景：

- `testConcurrentSelectSeat()` - 并发选座测试（座位冲突检测，20线程）
- `testStressSelectSeat1000Users()` - 压力测试（1000人同时选座）
- `testConcurrentCreateOrder()` - 并发创建订单测试
- `testConcurrentPayOrder()` - 并发支付测试（重复支付检测）

**测试配置**:
- 标准并发测试: 20线程 × 5请求 = 100并发
- 压力测试: 1000线程 × 1请求 = 1000并发（1000人同时选座）
- 支付测试: 20线程 = 20并发

### 8. StressTest - 压力测试

专门的压力测试类，测试高并发场景：

- `testStressSelectSeatSameSeat()` - 压力测试（1000人同时选座同一座位）
- `testStressSelectSeatDifferentSeats()` - 压力测试（1000人同时选座不同座位）

**测试配置**:
- 并发用户数: 1000
- 测试场景: 同一座位（测试冲突检测）、不同座位（测试吞吐量）
- 超时时间: 10分钟

### 8. PerformanceTest - 压力测试

进行高并发压力测试：

- `testMovieDetailPerformance()` - 电影详情接口压力测试
- `testMovieVersionListPerformance()` - 电影版本列表接口压力测试
- `testMovieShowTimePerformance()` - 电影场次接口压力测试

**测试配置**:
- 并发线程数: 50
- 每线程请求数: 100
- 总请求数: 5000
- 超时时间: 5 分钟

## 运行测试

### 方式一：使用 Maven Wrapper（推荐）

```bash
cd java-backend

# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=MovieApiTest
./mvnw test -Dtest=AppMovieApiTest
./mvnw test -Dtest=UserLoginTest
./mvnw test -Dtest=SeatSelectionTest
./mvnw test -Dtest=OrderTest
./mvnw test -Dtest=PaymentTest
./mvnw test -Dtest=SeatOrderConcurrentTest
./mvnw test -Dtest=StressTest
./mvnw test -Dtest=PerformanceTest

# 运行特定测试方法
./mvnw test -Dtest=MovieApiTest#testMovieDetail
```

### 方式二：使用 Maven

```bash
cd java-backend

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=MovieApiTest
mvn test -Dtest=AppMovieApiTest
mvn test -Dtest=PerformanceTest
```

### 方式三：在 IDE 中运行

1. **IntelliJ IDEA**:
   - 右键点击测试类或测试方法
   - 选择 "Run 'TestName'" 或 "Debug 'TestName'"

2. **Eclipse**:
   - 右键点击测试类或测试方法
   - 选择 "Run As" -> "JUnit Test"

3. **VS Code**:
   - 点击测试方法上方的 "Run Test" 链接

## 生成测试报告

### 生成 HTML 测试报告

```bash
cd java-backend

# 运行测试并生成报告
./mvnw surefire-report:report

# 报告位置
# target/site/surefire-report.html
```

### 查看测试覆盖率（需要配置 JaCoCo）

```bash
cd java-backend

# 运行测试并生成覆盖率报告
./mvnw clean test jacoco:report

# 报告位置
# target/site/jacoco/index.html
```

## 测试环境配置

### 测试配置文件

测试使用 `application-test.yml` 配置文件，请确保：

1. **数据库配置**: 测试数据库已配置并可访问
2. **Redis 配置**: 测试 Redis 已配置并可访问（如果使用）
3. **其他依赖**: 确保所有必要的服务都已启动

### 测试数据准备

运行测试前，请确保测试数据库中有以下测试数据：

- **用户账号**: 
  - 邮箱: `diy4869@gmail.com`
  - 密码: `123456`（经过2次MD5加密后存储）
  - 密码加密: 第一次MD5: `e10adc3949ba59abbe56e057f20f883e`
  - 密码加密: 第二次MD5（存储值）: `14e1b600b1fd579f47433b88e8d85291`
- 至少有一个电影记录（id=1 或其他有效 id）
- 至少有一个电影版本记录（movieId=34860 或其他有效 movieId）
- 相关的角色、演职员等测试数据

## 注意事项

### 1. 压力测试

压力测试会发送大量并发请求，请注意：

- 确保测试环境有足够的资源
- 压力测试可能需要较长时间（5-10 分钟）
- 建议在独立的测试环境中运行
- 注意监控数据库连接池和系统资源

### 2. 测试数据

- 测试中使用的一些 ID（如 movieId=1, movieId=34860）需要根据实际数据库调整
- 建议使用测试专用的数据库，避免影响生产数据
- 测试数据应该独立于生产数据

### 3. 测试环境

- 测试使用 `@ActiveProfiles("test")` 激活 test profile
- 确保 `application-test.yml` 配置正确
- 测试不会修改数据库数据（使用只读查询）

## 测试结果

测试结果会输出到控制台，包括：

- 测试通过/失败状态
- 响应内容（部分测试）
- 压力测试统计信息（吞吐量、响应时间等）

详细的测试报告请查看 `API_TEST_REPORT.md`。

## 故障排查

### 问题：测试失败 - 连接数据库失败

**解决方案**:
1. 检查 `application-test.yml` 中的数据库配置
2. 确保数据库服务已启动
3. 检查网络连接和防火墙设置

### 问题：测试失败 - MockMvc 初始化失败

**解决方案**:
1. 确保 `@SpringBootTest` 和 `@AutoConfigureMockMvc` 注解正确
2. 检查 Spring Boot 应用配置是否正确

### 问题：压力测试超时

**解决方案**:
1. 增加超时时间（修改 `latch.await()` 的参数）
2. 减少并发线程数或每线程请求数
3. 检查系统资源是否足够

## 扩展测试

### 添加新的测试用例

1. 在相应的测试类中添加新的测试方法
2. 使用 `@Test` 和 `@DisplayName` 注解
3. 使用 MockMvc 进行请求模拟
4. 使用断言验证结果

### 示例代码

```java
@Test
@DisplayName("测试新接口")
public void testNewApi() throws Exception {
    MvcResult result = mockMvc.perform(get("/api/new-endpoint")
                    .param("param", "value"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andReturn();
    
    System.out.println("响应内容: " + result.getResponse().getContentAsString());
}
```

## 测试覆盖率

### 生成测试覆盖率报告

项目配置了 JaCoCo 测试覆盖率插件，可以生成详细的覆盖率报告：

```bash
cd java-backend

# 运行所有测试并生成覆盖率报告
./mvnw clean test jacoco:report

# 查看覆盖率报告
# Windows: start target/site/jacoco/index.html
# Linux/Mac: open target/site/jacoco/index.html

# 检查覆盖率是否达到阈值（包级别 ≥ 60%，类级别 ≥ 50%）
./mvnw clean test jacoco:check
```

### 覆盖率阈值

- **包级别行覆盖率**: ≥ 60%
- **类级别行覆盖率**: ≥ 50%

### 覆盖率报告位置

- **HTML报告**: `target/site/jacoco/index.html`
- **XML报告**: `target/site/jacoco/jacoco.xml`
- **CSV报告**: `target/site/jacoco/jacoco.csv`

详细说明请参考 [测试覆盖率报告](./COVERAGE_TEST_REPORT.md)。

---

## 相关文档

### 测试报告

- [API 测试报告](./API_TEST_REPORT.md) - 通用API测试结果报告
- [选座测试报告](./SEAT_SELECTION_TEST_REPORT.md) - 选座功能测试结果报告
- [订单测试报告](./ORDER_TEST_REPORT.md) - 订单功能测试结果报告
- [支付测试报告](./PAYMENT_TEST_REPORT.md) - 支付功能测试结果报告
- [并发测试报告](./CONCURRENT_TEST_REPORT.md) - 并发测试结果报告
- [测试覆盖率报告](./COVERAGE_TEST_REPORT.md) - 测试覆盖率统计和分析
- [改善建议文档](./IMPROVEMENTS_RECOMMENDATIONS.md) - 系统改善建议和实施方案

### 技术文档

- [API 路径常量](../src/main/java/com/example/backend/constants/ApiPaths.java) - API 路径定义
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing) - Spring Boot 测试文档

## 联系与支持

如有问题，请联系开发团队或查看项目文档。

---

**最后更新**: 2025-01-17
