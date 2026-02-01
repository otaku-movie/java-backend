# JDK 21 配置说明

项目要求使用 **JDK 21**。若本地为 JDK 25 或其他版本，可能导致 JVM 崩溃等问题。

## 配置步骤

1. **安装 JDK 21**（若尚未安装）
   - 推荐：Zulu JDK 21、Eclipse Temurin 21、Amazon Corretto 21

2. **修改 VS Code 配置**
   - 打开 `.vscode/settings.json`
   - 在 `java.configuration.runtimes` 中，将 `path` 改为你本机 JDK 21 的安装路径
   - 示例：
     - Zulu: `C:\\Program Files\\Zulu\\zulu-21.0.1`
     - Temurin: `C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.1.12-hotspot`
     - Corretto: `C:\\Program Files\\Amazon Corretto\\jdk21.x.x_x`

3. **设置 JAVA_HOME**（可选，用于命令行 Maven）
   - 将 `JAVA_HOME` 环境变量指向 JDK 21 安装目录

4. **重启 VS Code** 以使配置生效
