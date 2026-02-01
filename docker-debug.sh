#!/bin/bash
# Docker 调试模式启动脚本（Linux/Mac）
# 设置 JAVA_DEBUG=true 并启动容器

echo "正在以调试模式启动 Docker 容器..."
echo ""
echo "提示："
echo "1. 容器启动后，等待应用完全启动（约 30-60 秒）"
echo "2. 在 VSCode 中选择 'Attach to Docker (Remote Debug)' 配置"
echo "3. 按 F5 连接到容器进行调试"
echo ""

export JAVA_DEBUG=true
docker-compose up --build -d

echo ""
echo "容器已启动，请检查日志："
echo "docker-compose logs -f backend"
echo ""
echo "如果看到 'Listening for transport dt_socket at address: 5005' 说明调试已启用"
echo ""
