# Phase 3 API 接口测试报告

> 日期：2026-06-15 22:53 | 测试工具：Python + urllib  
> 测试目标：订单结算服务全链路（登录→订单→费率→计算→结算→确认）

## 测试结果总览

```
==============================
PASS=11  FAIL=0  ERROR=0
==============================
```

11 项全部通过，0 失败。

---

## 详细结果

### 1. 认证

| # | 用例 | 请求 | 预期 | 实际 | 结果 |
|---|------|------|------|------|------|
| 1.1 | 正常登录 | `POST /auth/login` | code=200, 返回 token | code=200, admin | ✅ |

### 2. 订单 CRUD

| # | 用例 | 请求 | 预期 | 实际 | 结果 |
|---|------|------|------|------|------|
| 2.1 | 创建订单 | `POST /orders` | id>0, orderNo 非空 | id=7, ORD-20260615... | ✅ |
| 2.2 | 订单详情 | `GET /orders/{id}` | 客户名=TestCust | TestCust | ✅ |
| 2.3 | 订单列表 | `GET /orders?page=1&size=10` | total>=1 | total=7 | ✅ |

### 3. 费率规则

| # | 用例 | 请求 | 预期 | 实际 | 结果 |
|---|------|------|------|------|------|
| 3.1 | 创建规则 | `POST /fee-rules` | id>0 | id=5 | ✅ |
| 3.2 | 规则列表 | `GET /fee-rules?page=1&size=10` | total>=1 | total=5 | ✅ |

### 4. 批量费用计算

| # | 用例 | 请求 | 预期 | 实际 | 结果 |
|---|------|------|------|------|------|
| 4.1 | 计算费用 | `POST /orders/batch-calc` | success=1 | success=1 | ✅ |
| 4.2 | 验证结果 | `GET /orders/{id}` | feeAmount>0 | feeAmount=2000 | ✅ |

### 5. 结算单

| # | 用例 | 请求 | 预期 | 实际 | 结果 |
|---|------|------|------|------|------|
| 5.1 | 生成结算单 | `POST /settlements` | id>0 | id=3, STL-20260615-7 | ✅ |
| 5.2 | 结算单列表 | `GET /settlements?page=1&size=10` | total>=1 | total=3 | ✅ |
| 5.3 | 确认结算单 | `PUT /settlements/{id}/confirm` | code=200 | code=200 | ✅ |

---

## 测试覆盖的接口

| 方法 | 路径 | 结果 |
|------|------|------|
| POST | `/auth/login` | ✅ |
| POST | `/orders` | ✅ |
| GET | `/orders/{id}` | ✅ |
| GET | `/orders` | ✅ |
| POST | `/fee-rules` | ✅ |
| GET | `/fee-rules` | ✅ |
| POST | `/orders/batch-calc` | ✅ |
| POST | `/settlements` | ✅ |
| GET | `/settlements` | ✅ |
| PUT | `/settlements/{id}/confirm` | ✅ |

---

## 数据完整性验证

测试产生的数据：

| 表 | 关键字段 | 验证 |
|----|----------|------|
| t_order | orderNo=$(auto), customerName=TestCust, totalAmount=100000, feeAmount=2000 | ✅ |
| t_order_item | orderId=$order_id, productName=ProductA, amount=60000 | ✅ |
| t_fee_rule | ruleName=TestRate2pct, ruleType=1, ruleConfig=rate=0.02 | ✅ |
| t_settlement | settlementNo=STL-20260615-$order_id, settlementStatus=2 | ✅ |

**状态流转验证**：订单 1(待结算) → 计算费用 → 1(待结算) → 生成结算 → 2(已结算) ✅

---

## 测试脚本

`scripts/test-phase3.py`

```bash
# 执行方式
python scripts/test-phase3.py
```
