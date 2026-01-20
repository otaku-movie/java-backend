# 测试覆盖率报告

## 测试概述

本报告包含了对电影票选座、订单、支付系统的测试覆盖率统计和分析。

**测试日期**: 2025-01-17  
**测试工具**: JaCoCo  
**覆盖率目标**: 行覆盖率 ≥ 60%，类覆盖率 ≥ 50%  

---

## 1. 测试覆盖率配置

### 1.1 JaCoCo 插件配置

**位置**: `pom.xml`

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.60</minimum>
                            </limit>
                        </limits>
                    </rule>
                    <rule>
                        <element>CLASS</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.50</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 1.2 覆盖率阈值

| 指标 | 阈值 | 说明 |
|------|------|------|
| 包级别行覆盖率 | ≥ 60% | 所有包的平均行覆盖率 |
| 类级别行覆盖率 | ≥ 50% | 每个类的行覆盖率 |

---

## 2. 覆盖率统计

### 2.1 总体覆盖率

| 指标 | 覆盖率 | 状态 |
|------|--------|------|
| 指令覆盖率 (Instructions) | - | 待生成 |
| 分支覆盖率 (Branches) | - | 待生成 |
| 行覆盖率 (Lines) | - | 待生成 |
| 方法覆盖率 (Methods) | - | 待生成 |
| 类覆盖率 (Classes) | - | 待生成 |

### 2.2 模块覆盖率

#### 2.2.1 Controller 层覆盖率

| 模块 | 类数 | 行覆盖率 | 方法覆盖率 | 状态 |
|------|------|---------|-----------|------|
| MovieController | 1 | - | - | 待生成 |
| MovieShowTimeController | 1 | - | - | 待生成 |
| MovieOrderController | 1 | - | - | 待生成 |
| UserController | 1 | - | - | 待生成 |
| AppMovieController | 1 | - | - | 待生成 |

#### 2.2.2 Service 层覆盖率

| 模块 | 类数 | 行覆盖率 | 方法覆盖率 | 状态 |
|------|------|---------|-----------|------|
| MovieService | 1 | - | - | 待生成 |
| MovieOrderService | 1 | - | - | 待生成 |
| SelectSeatService | 1 | - | - | 待生成 |
| PaymentService | 1 | - | - | 待生成 |

#### 2.2.3 Mapper 层覆盖率

| 模块 | XML文件数 | 查询数 | 覆盖率 | 状态 |
|------|----------|--------|--------|------|
| MovieMapper | 1 | - | - | 待生成 |
| MovieShowTimeMapper | 1 | - | - | 待生成 |
| MovieOrderMapper | 1 | - | - | 待生成 |
| SelectSeatMapper | 1 | - | - | 待生成 |

---

## 3. 测试覆盖范围

### 3.1 选座功能覆盖

| 功能点 | 测试类 | 覆盖率 | 状态 |
|--------|--------|--------|------|
| 保存选座 | `SeatSelectionTest` | - | ✅ 已测试 |
| 取消选座 | `SeatSelectionTest` | - | ✅ 已测试 |
| 用户选座列表 | `SeatSelectionTest` | - | ✅ 已测试 |
| 选座列表 | `SeatSelectionTest` | - | ✅ 已测试 |
| 座位冲突检测 | `SeatSelectionTest`, `SeatOrderConcurrentTest` | - | ✅ 已测试 |

### 3.2 订单功能覆盖

| 功能点 | 测试类 | 覆盖率 | 状态 |
|--------|--------|--------|------|
| 创建订单 | `OrderTest` | - | ✅ 已测试 |
| 订单详情 | `OrderTest` | - | ✅ 已测试 |
| 取消订单 | `OrderTest` | - | ✅ 已测试 |
| 订单超时 | `OrderTest` | - | ✅ 已测试 |
| 我的票据 | `OrderTest` | - | ✅ 已测试 |

### 3.3 支付功能覆盖

| 功能点 | 测试类 | 覆盖率 | 状态 |
|--------|--------|--------|------|
| 支付订单 | `PaymentTest` | - | ✅ 已测试 |
| 支付成功场景 | `PaymentTest` | - | ✅ 已测试 |
| 支付失败场景 | `PaymentTest` | - | ✅ 已测试 |
| 重复支付检测 | `PaymentTest`, `SeatOrderConcurrentTest` | - | ✅ 已测试 |

### 3.4 并发场景覆盖

| 场景 | 测试类 | 并发数 | 覆盖率 | 状态 |
|------|--------|--------|--------|------|
| 并发选座（同一座位） | `SeatOrderConcurrentTest`, `StressTest` | 100, 1000 | - | ✅ 已测试 |
| 并发选座（不同座位） | `StressTest` | 1000 | - | ✅ 已测试 |
| 并发创建订单 | `SeatOrderConcurrentTest` | 100 | - | ✅ 已测试 |
| 并发支付 | `SeatOrderConcurrentTest` | 20 | - | ✅ 已测试 |

---

## 4. 未覆盖代码分析

### 4.1 未覆盖的类

待生成覆盖率报告后分析

### 4.2 未覆盖的方法

待生成覆盖率报告后分析

### 4.3 未覆盖的分支

待生成覆盖率报告后分析

---

## 5. 生成覆盖率报告

### 5.1 运行测试并生成覆盖率报告

```bash
cd java-backend

# 运行所有测试并生成覆盖率报告
./mvnw clean test jacoco:report

# 覆盖率报告位置
# target/site/jacoco/index.html
```

### 5.2 检查覆盖率阈值

```bash
# 运行测试并检查覆盖率阈值
./mvnw clean test jacoco:check
```

### 5.3 生成覆盖率聚合报告

```bash
# 生成详细的覆盖率报告
./mvnw jacoco:report
```

---

## 6. 覆盖率提升建议

### 6.1 增加单元测试

1. **Service 层测试**:
   - 为 `MovieService` 添加单元测试
   - 为 `MovieOrderService` 添加单元测试
   - 为 `SelectSeatService` 添加单元测试
   - 为 `PaymentService` 添加单元测试

2. **工具类测试**:
   - 为 `Utils` 添加单元测试
   - 为 `MessageUtils` 添加单元测试

3. **异常场景测试**:
   - 添加异常处理测试
   - 添加边界条件测试

### 6.2 增加集成测试

1. **完整流程测试**:
   - 选座 → 创建订单 → 支付的完整流程
   - 订单超时的完整流程
   - 订单取消的完整流程

2. **数据一致性测试**:
   - 事务回滚测试
   - 并发数据一致性测试

### 6.3 增加边界测试

1. **参数边界测试**:
   - 最大值、最小值测试
   - 空值、null值测试
   - 格式错误测试

2. **业务边界测试**:
   - 座位数量限制测试
   - 订单金额边界测试
   - 时间边界测试

---

## 7. 覆盖率目标

### 7.1 短期目标（1-2周）

| 模块 | 当前覆盖率 | 目标覆盖率 |
|------|-----------|-----------|
| Controller 层 | - | ≥ 80% |
| Service 层 | - | ≥ 70% |
| Mapper 层 | - | ≥ 60% |

### 7.2 中期目标（1个月）

| 模块 | 目标覆盖率 |
|------|-----------|
| 整体代码 | ≥ 70% |
| 核心业务代码 | ≥ 85% |
| 工具类 | ≥ 60% |

### 7.3 长期目标（3个月）

| 指标 | 目标值 |
|------|--------|
| 行覆盖率 | ≥ 80% |
| 分支覆盖率 | ≥ 75% |
| 方法覆盖率 | ≥ 80% |
| 类覆盖率 | ≥ 90% |

---

## 8. 覆盖率报告查看

### 8.1 HTML 报告

**位置**: `target/site/jacoco/index.html`

**内容**:
- 总体覆盖率统计
- 包级别覆盖率
- 类级别覆盖率
- 方法级别覆盖率
- 行级别覆盖率
- 分支覆盖率

### 8.2 XML 报告

**位置**: `target/site/jacoco/jacoco.xml`

**用途**:
- CI/CD 集成
- 覆盖率趋势分析
- 覆盖率报告生成

### 8.3 CSV 报告

**位置**: `target/site/jacoco/jacoco.csv`

**用途**:
- 数据分析和处理
- 报表生成

---

## 9. CI/CD 集成

### 9.1 GitHub Actions 集成

```yaml
name: Test Coverage

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests with coverage
        run: mvn clean test jacoco:report
      - name: Upload coverage reports
        uses: codecov/codecov-action@v2
        with:
          files: ./target/site/jacoco/jacoco.xml
```

### 9.2 Jenkins 集成

```groovy
stage('Test & Coverage') {
    steps {
        sh 'mvn clean test jacoco:report'
        publishHTML([
            reportDir: 'target/site/jacoco',
            reportFiles: 'index.html',
            reportName: 'JaCoCo Coverage Report'
        ])
    }
}
```

---

## 10. 覆盖率监控

### 10.1 覆盖率趋势

- 定期生成覆盖率报告
- 跟踪覆盖率变化趋势
- 设置覆盖率下降告警

### 10.2 覆盖率报告发布

- 将覆盖率报告发布到 CI/CD 平台
- 在代码审查时检查覆盖率
- 定期向团队汇报覆盖率情况

---

## 11. 运行测试生成覆盖率

### 11.1 生成覆盖率报告

```bash
cd java-backend

# 运行所有测试并生成覆盖率报告
./mvnw clean test jacoco:report

# 查看覆盖率报告
# Windows: start target/site/jacoco/index.html
# Linux/Mac: open target/site/jacoco/index.html
```

### 11.2 检查覆盖率阈值

```bash
# 检查覆盖率是否达到阈值
./mvnw clean test jacoco:check
```

### 11.3 生成覆盖率报告（仅报告）

```bash
# 如果已经运行过测试，可以直接生成报告
./mvnw jacoco:report
```

---

## 12. 覆盖率报告示例

### 12.1 总体覆盖率

```
JaCoCo Coverage Report

Total:
  Instructions:   75.3% (8,523 / 11,327)
  Branches:       68.9% (2,456 / 3,563)
  Lines:          74.2% (3,421 / 4,612)
  Methods:        82.1% (523 / 637)
  Classes:        88.5% (123 / 139)
```

### 12.2 包级别覆盖率

```
com.example.backend.controller:
  Instructions:   85.2%
  Branches:       78.5%
  Lines:          84.1%

com.example.backend.service:
  Instructions:   72.3%
  Branches:       65.8%
  Lines:          71.2%
```

---

## 13. 注意事项

### 13.1 覆盖率不是唯一指标

- 覆盖率只反映代码被执行的百分比
- 高覆盖率不等于高质量测试
- 需要关注测试的质量和有效性

### 13.2 覆盖率盲点

1. **边界条件**: 可能未被充分测试
2. **异常处理**: 异常分支可能未被覆盖
3. **并发场景**: 并发相关的代码可能难以覆盖

### 13.3 覆盖率目标设定

- 根据项目实际情况设定合理的覆盖率目标
- 核心业务代码应该有更高的覆盖率要求
- 工具类和辅助类可以有较低的覆盖率要求

---

## 14. 持续改进

### 14.1 定期审查

- 每周审查覆盖率报告
- 识别未覆盖的代码区域
- 制定测试计划提升覆盖率

### 14.2 测试质量提升

- 不仅关注覆盖率，更要关注测试质量
- 增加有意义的测试用例
- 减少无意义的测试用例

---

**报告生成时间**: 2025-01-17  
**报告版本**: v1.0  
**维护状态**: 持续更新

**注意**: 实际覆盖率数据需要在运行测试后生成。请运行 `./mvnw clean test jacoco:report` 生成最新的覆盖率报告。
