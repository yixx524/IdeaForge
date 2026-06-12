#!/bin/bash
# 在云服务器上运行，查看当前环境（学习部署 Step 0）
# 用法：bash server-inspect.sh  或  chmod +x server-inspect.sh && ./server-inspect.sh

set -e

echo "========== 系统 =========="
uname -a
lsb_release -a 2>/dev/null || cat /etc/os-release

echo ""
echo "========== 内存 / 磁盘 =========="
free -h
df -h /

echo ""
echo "========== Java =========="
if command -v java >/dev/null 2>&1; then
  java -version 2>&1
else
  echo "未安装 Java"
fi

echo ""
echo "========== Node.js（构建可选）=========="
if command -v node >/dev/null 2>&1; then
  node -v
  npm -v
else
  echo "未安装 Node.js"
fi

echo ""
echo "========== Maven =========="
if command -v mvn >/dev/null 2>&1; then
  mvn -v
else
  echo "未安装 Maven"
fi

echo ""
echo "========== PostgreSQL =========="
if command -v psql >/dev/null 2>&1; then
  psql --version
  systemctl is-active postgresql 2>/dev/null || systemctl is-active postgresql@* 2>/dev/null || echo "postgresql 服务状态未知"
else
  echo "未安装 PostgreSQL"
fi

echo ""
echo "========== Nginx =========="
if command -v nginx >/dev/null 2>&1; then
  nginx -v 2>&1
  systemctl is-active nginx 2>/dev/null || echo "nginx 未运行"
else
  echo "未安装 Nginx"
fi

echo ""
echo "========== Docker（区块链可能用到）=========="
if command -v docker >/dev/null 2>&1; then
  docker -v
  docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>/dev/null || echo "docker ps 需要权限"
else
  echo "未安装 Docker"
fi

echo ""
echo "========== 端口占用（80 / 443 / 8080 / 5432）=========="
ss -tlnp 2>/dev/null | grep -E ':(80|443|8080|5432)\s' || netstat -tlnp 2>/dev/null | grep -E ':(80|443|8080|5432)\s' || echo "请用 sudo ss -tlnp 查看"

echo ""
echo "========== 防火墙 =========="
if command -v ufw >/dev/null 2>&1; then
  sudo ufw status 2>/dev/null || ufw status 2>/dev/null || echo "ufw 状态需 sudo"
else
  echo "未安装 ufw"
fi

echo ""
echo "========== 完成 =========="
echo "请将以上输出复制给协助部署的人，以便决定还需安装哪些组件。"
