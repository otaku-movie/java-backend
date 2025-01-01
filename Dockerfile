# 第一阶段：构建应用程序
FROM maven:3.8.5-openjdk-17 AS build

# 创建工作目录
WORKDIR /movie/java-backend

# 复制 pom.xml 文件到镜像中，并下载 Maven 依赖
COPY pom.xml ./
RUN mvn dependency:go-offline -B  # 预先下载依赖以加快后续构建速度

# 复制源码到镜像中
COPY src ./src

# 构建应用程序
RUN mvn clean package -DskipTests  # 跳过测试以加快构建速度

# 第二阶段：运行应用程序
FROM openjdk:17-jdk-alpine

# 创建工作目录
WORKDIR /movie/server

# 复制构建阶段生成的 JAR 文件到运行时镜像中
COPY --from=build /movie/java-backend/target/backend-0.0.1-SNAPSHOT.jar /movie/server/app.jar

# 显示暴露端口
EXPOSE 8080

# 设置环境变量（可选，提供默认值）
ENV environment=dev

# 设置容器启动命令
ENTRYPOINT ["java", "-jar", "/movie/server/app.jar"]
