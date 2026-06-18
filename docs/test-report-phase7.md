# FinCoreCms 集成测试报告

## 测试概要

| 项 | 值 |
|---|-----|
| 测试日期 | 2026-06-17 |
| 测试范围 | Phase 3-5 全流程联调 |
| 测试脚本 | `scripts/test-e2e.py` |
| 结果 | **20/20 PASS（100%）** |

## 服务状态

| 服务 | 端口 | 状态 |
|------|------|------|
| fin-core-order | 8081 | ✅ |
| fin-core-payment | 8082 | ✅ |
| fin-core-fund | 8083 | ✅ |
| fin-core-report | 8084 | ✅ |
| fin-core-web (PC) | 5173 | ✅ |
| fin-core-miniapp | - | ✅ 手动验证 |

## 测试用例详情

### 1. 订单结算（5/5）

| 用例 | 方法 | 端点 | 结果 |
|------|------|------|------|
| 创建订单 | POST | `/orders` | ✅ |
| 订单状态=待结算 | GET | `/orders` | ✅ |
| 批量费用计算 | POST | `/orders/batch-calc` | ✅ |
| 生成结算单 | POST | `/settlements` | ✅ |
| 确认结算单 | PUT | `/settlements/{id}/confirm` | ✅ |

### 2. 收付款（8/8）

| 用例 | 方法 | 端点 | 结果 |
|------|------|------|------|
| 创建收款记录 | POST | `/receipts` | ✅ |
| 确认收款到账 | PUT | `/receipts/{id}/confirm` | ✅ |
| 发起付款申请 | POST | `/payments` | ✅ |
| 付款状态=待审批 | GET | `/payments` | ✅ |
| 生成账单 | POST | `/bills` | ✅ |
| 账单发送 | POST | `/bills/{id}/send` | ✅ |
| 待审批列表 | GET | `/approvals/pending` | ✅ |
| 审批通过 | PUT | `/approvals/{id}/process` | ✅ |

### 3. 资金与报表（7/7）

| 用例 | 方法 | 端点 | 结果 |
|------|------|------|------|
| 银行账户列表 | GET | `/accounts` | ✅ |
| 手动录入流水 | POST | `/transactions` | ✅ |
| 银行同步（Mock） | POST | `/transactions/batch-sync` | ✅ |
| 自动对账 | POST | `/reconciliations/run` | ✅ |
| 收支明细报表 | GET | `/reports/income-expense` | ✅ |
| 资金余额报表 | GET | `/reports/balance` | ✅ |
| 结算统计报表 | GET | `/reports/settlement-stats` | ✅ |

## 期间发现并修复的 Bug

| # | 问题 | 修复 |
|---|------|------|
| 1 | 订单号/账单号同秒碰撞（唯一索引冲突） | 编号加毫秒 `SSS` |
| 2 | PC 端分页 total 显示 `el.pagination.total` | Element Plus 国际化 `locale: zhCn` |
| 3 | 收付款页面空白 | Vite 代理缺 8082 路由 + 权限缺 `payment:view`/`fund:view` |
| 4 | 小程序请求 403 | 关闭域名校验 + 端口路由 |
| 5 | CSV 导出乱码 | 加 UTF-8 BOM + Content-Type 改 `text/csv` |
| 6 | 小程序支付已审批仍可操作 | 加状态判断，只允许待审批/审批中操作 |
| 7 | 审批通过需两次 | `totalNodes` 从 2 改为 1 |
| 8 | 导入 Mock → 真实 CSV 解析 | 实现 BOM 处理 + 行解析 |

## 单元测试

| 模块 | 测试类 | 用例数 | 结果 |
|------|--------|--------|------|
| fin-core-order | `OrderServiceTest` | 8 | ✅ 全通过 |
| fin-core-payment | `PaymentServiceTest` | 7 | ✅ 全通过 |
| fin-core-fund | `FundServiceTest` | 3 | ✅ 全通过 |
| **合计** | | **18** | **100%** |

- 框架: JUnit 5 + Mockito
- 方式: Mock Mapper 层，纯单元测试
- 执行: `mvn test -pl fin-core-order,fin-core-payment,fin-core-fund -am`

### OrderService（8 用例）
创建、分页、查单条、更新、更新不存在报错、删除置关闭、结算确认、结算作废回退

### PaymentService（7 用例）  
申请创建、分页、审批通过、审批状态错误报错、审批流不存在直接通过、驳回、驳回状态错误报错

### FundService（3 用例）
分页、对账处理、对账不存在报错

## 回归测试

运行 `python scripts/test-e2e.py` 可重复执行全流程验证。
