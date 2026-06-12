# IdeaForge 腾讯云部署指南

> **目标系统**：Ubuntu 22.04 LTS 64 位（jammy）  
> **目的**：学习部署流程，非长期生产运维  
> **前提**：服务器已快照，重装为全新 Ubuntu 22.04

---

## 环境要求

| 项 | 要求 |
|----|------|
| 云厂商 | 腾讯云 CVM |
| 操作系统 | **Ubuntu Server 22.04 LTS 64位** |
| 规格 | 2 核 4G / 60G SSD（当前配置即可） |
| 安全组入站 | `22`（SSH）、`80`（HTTP） |
| 本地开发机 | Windows，已安装 Node.js 20+、项目代码 |

## 架构

```
浏览器 → Nginx:80 → Vue dist 静态文件
                 → /api/* → Spring Boot:8080 → PostgreSQL:5432 (localhost)
```

| 环境 | 数据库连接 | 配置方式 |
|------|-----------|----------|
| 本地开发 | 虚拟机 `192.168.226.131` | 默认 `application.yml` |
| 云服务器 | 本机 `127.0.0.1` | `SPRING_PROFILES_ACTIVE=prod` → `application-prod.yml` |

---

## 阶段一览

| 阶段 | 在哪里 | 内容 |
|------|--------|------|
| 0 | 腾讯云控制台 + SSH | 重装 22.04、开放安全组、登录验证 |
| 1 | 云服务器 | JDK 21、PostgreSQL 16、Nginx |
| 2 | 虚拟机 | 导出旧数据库（可选） |
| 3 | 云服务器 | 导入数据库或初始化空库 |
| 4 | 本地 Windows | 构建 JAR + 前端 dist |
| 5 | 云服务器 | 上传、配置 systemd + Nginx |
| 6 | 浏览器 | 验证 |

---

## 阶段 0：腾讯云重装与登录

### 0.1 控制台重装系统

1. 腾讯云 CVM → 实例 → **重装系统**
2. 镜像选择：**Ubuntu Server 22.04 LTS 64位**
3. 系统盘 60G 保持不变即可
4. 设置 root 密码（或绑定 SSH 密钥）

### 0.2 安全组

实例 → 安全组 → 入站规则，确保有：

| 端口 | 协议 | 来源 | 用途 |
|------|------|------|------|
| 22 | TCP | 你的 IP 或 0.0.0.0/0 | SSH |
| 80 | TCP | 0.0.0.0/0 | HTTP 访问 |

**不要**对公网开放 `8080`、`5432`。

### 0.3 SSH 登录并确认系统版本

```bash
ssh root@你的云服务器公网IP
```

登录后执行：

```bash
lsb_release -a
uname -m
free -h
df -h /
```

**预期输出**（大致如下即可）：

```
Distributor ID: Ubuntu
Description:    Ubuntu 22.04.x LTS
Release:        22.04
Codename:       jammy
```

若不是 `22.04` / `jammy`，请停止并确认镜像是否选错。

---

## 阶段 1：安装基础环境（Ubuntu 22.04）

以下命令均在**云服务器**上执行，建议逐小节完成并确认无报错。

### 1.1 系统更新

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl wget gnupg lsb-release ufw ca-certificates
```

### 1.2 安装 JDK 21（Eclipse Temurin）

Ubuntu 22.04 默认不带 Java 21，使用 Adoptium 源：

```bash
sudo install -d /etc/apt/keyrings
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | \
  sudo tee /etc/apt/keyrings/adoptium.asc > /dev/null
echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb jammy main" | \
  sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-21-jdk
java -version
```

预期：`openjdk version "21.x.x"`

### 1.3 安装 PostgreSQL 16 + pgvector

```bash
sudo install -d /usr/share/postgresql-common/pgdg
sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail \
  https://www.postgresql.org/media/keys/ACCC4CF8.asc
echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt jammy-pgdg main" | \
  sudo tee /etc/apt/sources.list.d/pgdg.list
sudo apt update
sudo apt install -y postgresql-16 postgresql-16-pgvector
sudo systemctl enable postgresql
sudo systemctl start postgresql
psql --version
```

预期：`psql (PostgreSQL) 16.x`

### 1.4 创建数据库与用户

将 `YOUR_DB_PASSWORD` 替换为你的密码（**阶段 5 的 `.env` 里要用同一个**）：

```bash
sudo -u postgres psql <<'EOF'
CREATE USER yi_ideaforge WITH PASSWORD 'YOUR_DB_PASSWORD';
CREATE DATABASE knowledge_db OWNER yi_ideaforge;
\c knowledge_db
CREATE EXTENSION IF NOT EXISTS vector;
GRANT ALL ON SCHEMA public TO yi_ideaforge;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO yi_ideaforge;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO yi_ideaforge;
EOF
```

验证连接：

```bash
psql -h localhost -U yi_ideaforge -d knowledge_db -c "SELECT extname FROM pg_extension WHERE extname = 'vector';"
```

### 1.5 安装 Nginx

```bash
sudo apt install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
nginx -v
```

浏览器访问 `http://云服务器公网IP/`，应看到 Nginx 默认欢迎页。

### 1.6 创建应用目录

```bash
sudo useradd -r -s /bin/false ideaforge 2>/dev/null || true
sudo mkdir -p /opt/ideaforge /var/www/ideaforge
sudo chown ideaforge:ideaforge /opt/ideaforge
sudo chown -R www-data:www-data /var/www/ideaforge
```

### 1.7 防火墙

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw --force enable
sudo ufw status
```

### 1.8 阶段 1 完成检查

```bash
java -version
psql --version
nginx -v
systemctl is-active postgresql nginx
free -h
```

**阶段 1 完成后**，可继续阶段 2/3（有旧数据）或跳到阶段 4（先构建，空库稍后再初始化）。

---

## 阶段 2：虚拟机 — 导出数据库（有旧数据时）

在**虚拟机**（`192.168.226.131`）执行：

```bash
pg_dump -h localhost -U yi_ideaforge -d knowledge_db -F c -f ~/ideaforge.dump
ls -lh ~/ideaforge.dump
```

传到云服务器（在虚拟机或本地 Windows 执行，改 IP）：

```bash
scp ~/ideaforge.dump root@云服务器公网IP:/tmp/
```

Windows PowerShell 示例：

```powershell
scp \\wsl$\Ubuntu\home\你的用户\ideaforge.dump root@云服务器IP:/tmp/
# 或先从虚拟机下载到 Windows 再 scp
```

---

## 阶段 3：云服务器 — 导入或初始化数据库

### 方案 A：导入虚拟机数据（推荐，保留现有条目）

```bash
pg_restore -h localhost -U yi_ideaforge -d knowledge_db --no-owner --no-acl /tmp/ideaforge.dump

psql -h localhost -U yi_ideaforge -d knowledge_db -c "\dt"
psql -h localhost -U yi_ideaforge -d knowledge_db -c "SELECT count(*) FROM knowledge_items;"
```

少量 warning 通常可忽略；两张表存在且有数据即可。

### 方案 B：空库启动（不迁移旧数据）

在云服务器项目目录执行（需先把 `db/init-schema.sql` 上传到服务器，或从本机 scp）：

```bash
# 本地 Windows 上传 schema 文件
# scp D:\Code\IdeaForge-v\deploy\init-schema.sql root@云服务器IP:/tmp/

psql -h localhost -U yi_ideaforge -d knowledge_db -f /tmp/init-schema.sql
psql -h localhost -U yi_ideaforge -d knowledge_db -c "\dt"
```

应看到 `knowledge_items`、`idea_categories`，以及预置类别数据。

---

## 阶段 4：本地 Windows — 构建

```powershell
cd D:\Code\IdeaForge-v\IdeaForge
.\mvnw.cmd clean package -DskipTests

cd ..\ideaforge-ui
npm install
npm run build
```

产物：

- `IdeaForge\target\IdeaForge-0.0.1-SNAPSHOT.jar`
- `ideaforge-ui\dist\` 下全部文件

---

## 阶段 5：上传与配置

### 5.1 从 Windows 上传（改 `云服务器IP`）

```powershell
scp D:\Code\IdeaForge-v\IdeaForge\target\IdeaForge-0.0.1-SNAPSHOT.jar root@云服务器IP:/opt/ideaforge/
scp -r D:\Code\IdeaForge-v\ideaforge-ui\dist\* root@云服务器IP:/var/www/ideaforge/
scp D:\Code\IdeaForge-v\deploy\ideaforge.service root@云服务器IP:/tmp/
scp D:\Code\IdeaForge-v\deploy\nginx-ideaforge.conf root@云服务器IP:/tmp/
scp D:\Code\IdeaForge-v\deploy\env.example root@云服务器IP:/tmp/
```

### 5.2 配置环境变量

```bash
sudo cp /tmp/env.example /opt/ideaforge/.env
sudo nano /opt/ideaforge/.env
```

内容示例：

```env
SPRING_PROFILES_ACTIVE=prod
DEEPSEEK_API_KEY=sk-你的密钥
DB_PASSWORD=YOUR_DB_PASSWORD
```

```bash
sudo chmod 600 /opt/ideaforge/.env
sudo chown ideaforge:ideaforge /opt/ideaforge/.env
sudo chown ideaforge:ideaforge /opt/ideaforge/IdeaForge-0.0.1-SNAPSHOT.jar
sudo chown -R www-data:www-data /var/www/ideaforge
```

### 5.3 启动 Spring Boot（systemd）

```bash
sudo cp /tmp/ideaforge.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable ideaforge
sudo systemctl start ideaforge
sudo systemctl status ideaforge
```

日志：

```bash
sudo journalctl -u ideaforge -n 50 --no-pager
sudo journalctl -u ideaforge -f
```

### 5.4 配置 Nginx

```bash
sudo cp /tmp/nginx-ideaforge.conf /etc/nginx/sites-available/ideaforge
sudo ln -sf /etc/nginx/sites-available/ideaforge /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx
```

---

## 阶段 6：验证

```bash
curl http://127.0.0.1:8080/api/health
curl http://127.0.0.1/api/health
```

浏览器打开 `http://云服务器公网IP/`：

1. 首页正常  
2. `/create` → 输入文字 → AI 流式整理  
3. 保存 → `/browse` 能搜到  

---

## 常见问题（Ubuntu 22.04）

| 现象 | 处理 |
|------|------|
| `apt update` GPG 报错 | 检查 keyring 路径与 `jammy` 代号是否正确 |
| Java 版本不是 21 | 确认装的是 `temurin-21-jdk`，非 `default-jdk` |
| Spring Boot 启动报 schema 校验失败 | 数据库表未建好，回到阶段 3 |
| 连不上数据库 | 检查 `.env` 中 `DB_PASSWORD` 与 1.4 是否一致 |
| AI 整理一直 loading | Nginx 需 `proxy_buffering off`（模板已含） |
| 502 Bad Gateway | `systemctl status ideaforge`，确认 8080 在监听 |
| 内存紧张 | `free -h`；JVM 已限 `-Xmx512m` |

---

## 部署文件索引

| 文件 | 用途 |
|------|------|
| `IdeaForge/src/main/resources/application-prod.yml` | 云服务器 Spring 配置 |
| `deploy/env.example` | `/opt/ideaforge/.env` 模板 |
| `deploy/ideaforge.service` | systemd 服务 |
| `deploy/nginx-ideaforge.conf` | Nginx 站点 |
| `deploy/init-schema.sql` | 空库建表 + 种子类别 |
| `deploy/server-inspect.sh` | 装完环境后可选自检 |

---

## 下一步

重装 **Ubuntu 22.04 LTS** 并 SSH 登录后，从 **阶段 0.3** 验证系统版本，再执行 **阶段 1.1**。

每完成一个阶段，有问题把命令输出贴出来即可继续。
