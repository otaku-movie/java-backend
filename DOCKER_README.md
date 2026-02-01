# Docker 部署指南

本文档说明如何使用 Docker 部署后端服务。

## 前置要求

- Docker 20.10+
- Docker Compose 2.0+

## 快速开始

### 1. 开发环境（仅启动依赖服务，后端本地跑）

只启动 PostgreSQL、Redis、RabbitMQ，后端在本地运行：

```bash
docker-compose -f docker-compose.dev.yml up -d
```

**若出现 `Unable to connect to Redis` 错误**，可先仅启动 Redis：

```bash
docker-compose -f docker-compose.redis-only.yml up -d
```

访问地址：
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- RabbitMQ 管理界面: http://localhost:15672 (guest/guest)

### 2. 完整环境（包含后端服务，使用容器内数据库）

启动所有服务，包括后端应用：

```bash
docker-compose up -d
```

或者构建并启动：

```bash
docker-compose up -d --build
```

### 清理缓存构建（推荐）

每次构建都清理缓存，确保使用最新的代码和依赖。脚本会自动询问是否启动服务：

**Linux/Mac:**
```bash
./docker-build.sh
```

**Windows:**
```cmd
docker-build.bat
```

**或者直接使用命令:**
```bash
# 仅构建（清理缓存）
docker-compose build --no-cache backend

# 构建并启动
docker-compose build --no-cache backend && docker-compose up -d
```

### 3. 生产环境

使用生产环境配置：

```bash
# 创建环境变量文件
cp .env.example .env
# 编辑 .env 文件，设置生产环境变量

# 启动服务
docker-compose -f docker-compose.prod.yml up -d --build
```

## 服务说明

### 后端服务 (backend)

- **端口**: 8080
- **健康检查**: 通过访问 Swagger UI 判断：`http://localhost:8080/swagger-ui/index.html`
- **API 文档**: `http://localhost:8080/swagger-ui.html`
- **环境变量**:
  - `SPRING_PROFILES_ACTIVE`: 激活的配置文件 (dev/test/prod)
  - `SPRING_DATASOURCE_URL`: 数据库连接 URL
  - `SPRING_DATA_REDIS_HOST`: Redis 主机地址
  - `SPRING_RABBITMQ_HOST`: RabbitMQ 主机地址

### PostgreSQL

- **端口**: 5432
- **数据库**: `test_movie`
- **用户名**: `postgres`
- **密码**: `postgre` (开发环境)
- **数据持久化**: `postgres_data` volume（仅在使用容器内数据库时）

> 💡 **宿主机数据库模式**
>
> 当前 `docker-compose.yml` 中已支持让后端容器连接宿主机上的 PostgreSQL：
>
> - 宿主机数据库地址：`localhost:5432`
> - 容器内访问宿主机：`host.docker.internal:5432`
> - 后端通过环境变量配置：
>   ```yaml
>   SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/test_movie?...
>   ```
>
> 使用这种模式时，可以选择：
> - 不启动 `postgres` 服务，仅启动 `backend / redis / rabbitmq`
> - 或保留 `postgres` 服务但不被后端使用（方便切换模式）

### Redis

- **端口**: 6379
- **数据持久化**: `redis_data` volume
- **AOF 持久化**: 已启用

### RabbitMQ

- **AMQP 端口**: 5672
- **管理界面端口**: 15672
- **默认用户名**: guest
- **默认密码**: guest
- **管理界面**: http://localhost:15672
- **数据持久化**: `rabbitmq_data` volume
- **已安装插件**: `rabbitmq_delayed_message_exchange` (延迟消息交换机插件)

## 常用命令

### 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 启动并查看日志
docker-compose up

# 启动并重新构建
docker-compose up -d --build
```

### 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v
```

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看后端服务日志
docker-compose logs -f backend

# 查看最近 100 行日志
docker-compose logs --tail=100 backend
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
```

### 进入容器

```bash
# 进入后端容器
docker exec -it movie-backend sh

# 进入 PostgreSQL 容器
docker exec -it movie-postgres psql -U postgres -d test_movie

# 进入 Redis 容器
docker exec -it movie-redis redis-cli
```

### 远程调试模式

Docker 容器默认已启用远程调试（`JAVA_DEBUG=true`），可以通过 VSCode 连接进行断点调试。

#### 启动调试模式

**Windows:**
```cmd
docker-debug.bat
```

**Linux/Mac:**
```bash
chmod +x docker-debug.sh
./docker-debug.sh
```

**或者手动设置环境变量:**
```bash
# Windows
set JAVA_DEBUG=true
docker-compose up --build -d

# Linux/Mac
export JAVA_DEBUG=true
docker-compose up --build -d
```

#### 连接调试器

1. **等待容器启动完成**（约 30-60 秒）
   ```bash
   docker-compose logs -f backend
   ```
   看到 `Listening for transport dt_socket at address: 5005` 说明调试已启用

2. **在 VSCode 中连接调试器**:
   - 按 `F5` 或点击调试面板
   - 选择 **"Attach to Docker (Remote Debug)"** 配置
   - 点击运行按钮

3. **设置断点**:
   - 在代码中点击行号左侧设置断点
   - 触发相应的 API 请求
   - 程序会在断点处暂停

#### 调试端口

- **调试端口**: `5005`（已映射到宿主机）
- **应用端口**: `8080`

#### 关闭调试模式

如果不需要调试，可以设置 `JAVA_DEBUG=false`:
```bash
# Windows
set JAVA_DEBUG=false
docker-compose up -d

# Linux/Mac
export JAVA_DEBUG=false
docker-compose up -d
```

### 查看服务状态

```bash
# 查看运行中的服务
docker-compose ps

# 查看服务健康状态
docker-compose ps --format json | jq '.[] | {name: .Name, status: .State}'
```

## 环境变量配置

### 开发环境

开发环境使用 `docker-compose.dev.yml`，配置已硬编码在文件中。

另外，根目录下提供了一个简单的数据库创建脚本：

- 路径：`src/main/resources/sql/createDATABASE.sql`
- 作用：只负责创建 `test_movie` 数据库，不建表
- 适用场景：使用容器内 PostgreSQL 时，初始化数据库结构前先创建数据库

### 生产环境

生产环境使用环境变量文件 `.env`，示例：

```env
# 数据库配置
POSTGRES_DB=test_movie
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password
POSTGRES_PORT=5432

# Redis 配置
REDIS_PASSWORD=your_redis_password
REDIS_PORT=6379

# RabbitMQ 配置
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=your_rabbitmq_password
RABBITMQ_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672

# 后端配置
BACKEND_PORT=8080
```

## 数据持久化

所有数据都存储在 Docker volumes 中：

- `postgres_data`: PostgreSQL 数据
- `redis_data`: Redis 数据
- `rabbitmq_data`: RabbitMQ 数据

### 备份数据

```bash
# 备份 PostgreSQL
docker exec movie-postgres pg_dump -U postgres test_movie > backup.sql

# 备份 Redis
docker exec movie-redis redis-cli SAVE
docker cp movie-redis:/data/dump.rdb ./redis-backup.rdb
```

### 恢复数据

```bash
# 恢复 PostgreSQL
cat backup.sql | docker exec -i movie-postgres psql -U postgres -d test_movie
```

## 故障排查

### 后端无法连接数据库

1. 检查 PostgreSQL 是否正常运行：
   ```bash
   docker-compose ps postgres
   ```

2. 检查网络连接：
   ```bash
   docker exec movie-backend ping postgres
   ```

3. 查看后端日志：
   ```bash
   docker-compose logs backend
   ```

### 后端无法连接 Redis

1. 检查 Redis 是否正常运行：
   ```bash
   docker-compose ps redis
   ```

2. 测试 Redis 连接：
   ```bash
   docker exec movie-redis redis-cli ping
   ```

### 后端无法连接 RabbitMQ

1. 检查 RabbitMQ 是否正常运行：
   ```bash
   docker-compose ps rabbitmq
   ```

2. 访问 RabbitMQ 管理界面：http://localhost:15672

### 查看容器资源使用

```bash
# 查看所有容器资源使用情况
docker stats

# 查看特定容器资源使用
docker stats movie-backend
```

## 性能优化

### JVM 参数调整

在 `docker-compose.yml` 中修改 `JAVA_OPTS` 环境变量：

```yaml
environment:
  - JAVA_OPTS=-Xms1024m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### 数据库连接池

在 `application.yml` 中配置数据库连接池大小。

## 安全建议

1. **生产环境必须修改默认密码**
2. **使用环境变量文件管理敏感信息**
3. **限制端口暴露**（生产环境建议使用反向代理）
4. **定期更新镜像版本**
5. **启用 SSL/TLS 连接**（生产环境）

## 更新服务

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose up -d --build

# 或者只更新后端服务
docker-compose up -d --build backend
```

## 清理

```bash
# 停止并删除容器
docker-compose down

# 停止并删除容器和数据卷
docker-compose down -v

# 清理未使用的镜像
docker image prune -a
```
