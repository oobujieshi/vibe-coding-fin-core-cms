# FinCoreCms - 财务管理模块

基于 Spring Boot 3.2 微服务 + Vue 3 + 微信小程序的财务管理平台。

## 功能模块

| 模块 | 功能 | 端口 |
|------|------|------|
| **订单结算** | 订单 CRUD、费率规则、批量费用计算、结算单管理 | 8081 |
| **收付款** | 收款管理、付款多级审批、账单生成/合并/拆分、扫码核销 | 8082 |
| **资金流水** | 银行账户管理、流水录入/同步、自动对账引擎 | 8083 |
| **报表中心** | 收支明细、资金余额、结算统计 | 8084 |
| **PC 前端** | 14 个功能页面，Element Plus UI | 5173 |
| **小程序** | 账单查询、支付确认、扫码核销、审批处理、报表摘要 | - |

## 技术栈

| 层 | 技术 |
|----|------|
| 框架 | Spring Boot 3.2.5 + Spring Cloud Alibaba |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.x |
| 消息队列 | RabbitMQ 3.12 |
| 注册中心 | Nacos 2.3 |
| 安全 | Spring Security + JWT + RBAC |
| 前端 | Vue 3 + Element Plus 2.9 + Vite 6 |
| 小程序 | 微信原生框架 |
| 日志 | Logback（按天切割，保留 90 天） |
| 测试 | JUnit 5 + Mockito |

## 快速开始

### 环境要求

- JDK 21
- Maven 3.9+
- MySQL 8.0
- Redis（可选）
- RabbitMQ（可选）
- Node.js 22+

### 一键启动

```bash
# 编译 + 启动全部 5 个服务
start-all.bat

# 或单独启动前端
start-frontend.bat
```

### 访问

| 地址 | 说明 |
|------|------|
| http://localhost:5173 | PC 前端 |
| http://localhost:8081 | 订单服务 |
| http://localhost:8082 | 支付服务 |
| http://localhost:8083 | 资金服务 |
| http://localhost:8084 | 报表服务 |

**默认账号**：`admin` / `admin123`

## 项目结构

```
FinCoreCms/
├── fin-core-common/          # 公共模块（Entity/DTO/Result/Security）
├── fin-core-order/           # 订单服务 8081
├── fin-core-payment/         # 支付服务 8082
├── fin-core-fund/            # 资金服务 8083
├── fin-core-report/          # 报表服务 8084
├── fin-core-gateway/         # 网关（预留）
├── fin-core-web/             # PC 前端 Vue 3
├── fin-core-miniapp/         # 微信小程序
├── docs/                     # 文档
│   ├── user-guide-pc.md      # PC 端操作手册
│   ├── user-guide-miniapp.md # 小程序操作手册
│   ├── deployment-guide.md   # 部署指南
│   └── test-report-phase7.md # 测试报告
├── scripts/                  # 脚本
│   ├── test-e2e.py           # E2E 测试（20 用例）
│   └── rebuild-linear.py     # Linear 同步工具
├── sql/                      # DDL 脚本
├── start-all.bat             # 一键启动
└── Dockerfile.*              # Docker 镜像
```

## 测试

| 类型 | 用例 | 命令 |
|------|------|------|
| 单元测试 | 18 用例 | `mvn test -pl fin-core-order,fin-core-payment,fin-core-fund -am` |
| E2E 测试 | 20 用例 | `python scripts/test-e2e.py` |

## 文档

- [PC 端操作手册](docs/user-guide-pc.md)
- [小程序操作手册](docs/user-guide-miniapp.md)
- [部署指南](docs/deployment-guide.md)
- [测试报告](docs/test-report-phase7.md)
- [API 规范](docs/api-specification.md)
- [执行计划](财务管理模块执行计划.md)
- [开发方案](财务管理模块开发方案.md)
