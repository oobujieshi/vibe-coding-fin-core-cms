# FinCoreCms 部署指南

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 21 | Eclipse Temurin 推荐 |
| Maven | 3.9+ | |
| MySQL | 8.0 | 数据库 `fincore` |
| Redis | 7.0+ | 可选（缓存） |
| RabbitMQ | 3.12+ | 可选（消息队列） |
| Node.js | 22+ | 前端 + 小程序开发 |
| Docker | 24+ | 可选（容器化部署） |

## 本地开发

### 1. 启动基础设施
```bash
docker-compose up -d mysql redis rabbitmq
```

### 2. 初始化数据库
```bash
docker exec -i fincore-mysql mysql -uroot -proot123456 fincore < sql/init/01-create-tables.sql
```

### 3. 编译项目
```bash
set JAVA_HOME=<jdk21-path>
mvn install -DskipTests
```

### 4. 启动微服务
```bash
# 终端1: Order (8081)
mvn spring-boot:run -f fin-core-order/pom.xml

# 终端2: Payment (8082)
mvn spring-boot:run -f fin-core-payment/pom.xml

# 终端3: Fund (8083)
mvn spring-boot:run -f fin-core-fund/pom.xml

# 终端4: Report (8084)
mvn spring-boot:run -f fin-core-report/pom.xml

# 终端5: Frontend (5173)
cd fin-core-web && npm install && npm run dev
```

### 5. 访问
- PC 端: http://localhost:5173
- 账号: admin / admin123

## Docker 部署

### 构建镜像
```bash
mvn package -DskipTests
docker build -t fincore-order   -f Dockerfile.order .
docker build -t fincore-payment -f Dockerfile.payment .
docker build -t fincore-fund    -f Dockerfile.fund .
docker build -t fincore-report  -f Dockerfile.report .
```

### 启动
```bash
docker run -d -p 8081:8081 --network fincore fincore-order
docker run -d -p 8082:8082 --network fincore fincore-payment
docker run -d -p 8083:8083 --network fincore fincore-fund
docker run -d -p 8084:8084 --network fincore fincore-report
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| fin-core-order | 8081 | 订单/结算/费率规则 |
| fin-core-payment | 8082 | 收款/付款/账单/审批 |
| fin-core-fund | 8083 | 账户/流水/对账 |
| fin-core-report | 8084 | 报表中心 |
| fin-core-web | 5173 | PC 前端（开发） |
| fin-core-miniapp | - | 小程序（微信开发者工具） |

## 生产环境 Checklist

- [ ] 修改默认密码 admin/admin123
- [ ] 配置 JWT 密钥（生产随机生成）
- [ ] 启用 Nacos 注册中心
- [ ] 配置 HTTPS
- [ ] 配置数据库连接池（HikariCP）
- [ ] 配置日志级别为 WARN
- [ ] 配置监控（Spring Boot Actuator）
- [ ] 配置备份策略（数据库 + 日志）
