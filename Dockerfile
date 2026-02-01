# 第一阶段：构建应用程序
# 使用 Amazon Corretto (OpenJDK) 21 LTS 版本
FROM amazoncorretto:21 AS build

# 设置工作目录
WORKDIR /movie/java-backend

# 先复制 pom.xml
COPY pom.xml ./

# 安装必要的工具和 Maven 3.9.x（满足 Spring Boot 3.2.4 要求）
RUN yum install -y wget tar gzip && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -O /tmp/maven.tar.gz && \
    mkdir -p /opt && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -sf /opt/apache-maven-3.9.6 /opt/maven && \
    rm -f /tmp/maven.tar.gz && \
    yum clean all

# 设置 Maven 环境变量
ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B || mvn dependency:resolve -B

# 复制源代码
COPY src ./src

# 强制清理所有旧的构建文件和缓存（确保完全清理）
# 删除 target 目录、所有 .class 文件、Maven 本地仓库缓存等
RUN echo "清理旧的构建文件..." && \
    rm -rf target/ .mvn/wrapper/maven-wrapper.jar ~/.m2/repository/com/example/backend || true && \
    find . -type f -name "*.class" -delete 2>/dev/null || true && \
    find . -type f -name "*.jar" -path "*/target/*" -delete 2>/dev/null || true && \
    echo "清理完成，开始构建..."

# 构建应用程序（跳过测试和插件验证）
# 使用 -U 强制更新依赖，确保使用最新代码
# 使用 -e 显示错误详情，-X 显示详细日志（可选，用于调试）
RUN echo "开始 Maven 构建..." && \
    mvn clean compile package -DskipTests -B -Dmaven.plugin.validation=NONE -U && \
    echo "构建完成，检查编译结果..." && \
    ls -la target/classes/com/example/backend/entity/ || true

# 第二阶段：运行应用程序
# 使用 Amazon Corretto (OpenJDK) 21 LTS 版本
FROM amazoncorretto:21

# 安装必要的工具（用于健康检查）和用户管理工具
RUN yum install -y wget shadow-utils && yum clean all

# 创建非 root 用户
RUN groupadd -r spring && useradd -r -g spring spring

# 设置工作目录
WORKDIR /movie/server

# 从构建阶段复制 JAR 文件
COPY --from=build /movie/java-backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

# 更改文件所有者
RUN chown spring:spring app.jar

# 切换到非 root 用户
USER spring:spring

# 暴露端口
EXPOSE 8080
# 暴露调试端口
EXPOSE 5005

# 设置 JVM 参数
# 支持远程调试：-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV JAVA_DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# 健康检查（使用 SpringDoc 3.x 的正确路径）
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/swagger-ui/index.html || wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动应用（支持调试模式）
# 如果设置了 JAVA_DEBUG=true，则启用远程调试
ENTRYPOINT ["sh", "-c", "if [ \"$JAVA_DEBUG\" = \"true\" ]; then java $JAVA_OPTS $JAVA_DEBUG_OPTS -jar app.jar; else java $JAVA_OPTS -jar app.jar; fi"]
