# FinCoreCms API 接口规范

> 版本：1.0.0 | 更新日期：2026-06-15
> 本文档为前后端接口契约，开发前必读，变更需双方确认。

---

## 1. 通用规范

### 1.1 基础信息

| 项 | 值 |
|----|-----|
| Base URL | `/api/v1` |
| 认证方式 | Header `Authorization: Bearer <JWT_TOKEN>` |
| Content-Type | `application/json; charset=utf-8` |

### 1.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1718345600000
}
```

### 1.3 分页请求

```
?page=1&size=20&sort=create_time,desc
```

### 1.4 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  }
}
```

### 1.5 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未认证 / Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如状态不允许操作） |
| 500 | 服务器内部错误 |

---

## 2. 认证接口 `/api/v1/auth`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/auth/login` | 登录获取 Token | 公开 |
| POST | `/auth/refresh` | 刷新 Token | 需认证 |
| POST | `/auth/logout` | 登出（Token 加入黑名单） | 需认证 |

### POST `/auth/login`

请求：
```json
{
  "username": "admin",
  "password": "123456"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbG...",
    "refreshToken": "eyJhbG...",
    "expiresIn": 7200,
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "roles": ["ADMIN"],
      "permissions": ["order:create", "order:view", "..."]
    }
  }
}
```

---

## 3. 订单结算服务 `/api/v1/orders`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/orders` | 分页查询订单列表 | FINANCE, BIZ |
| GET | `/orders/{id}` | 订单详情 | FINANCE, BIZ |
| POST | `/orders` | 创建订单 | BIZ, FINANCE |
| PUT | `/orders/{id}` | 更新订单 | BIZ, FINANCE |
| DELETE | `/orders/{id}` | 删除订单（逻辑删除） | ADMIN |
| PUT | `/orders/{id}/status` | 更新订单状态 | FINANCE |
| POST | `/orders/batch-calc` | 批量费用计算 | FINANCE |
| POST | `/orders/import` | Excel 批量导入 | FINANCE |
| GET | `/orders/export` | Excel 批量导出 | FINANCE |
| GET | `/settlements` | 结算单列表 | FINANCE |
| GET | `/settlements/{id}` | 结算单详情 | FINANCE |
| POST | `/settlements` | 生成结算单 | FINANCE |
| PUT | `/settlements/{id}/confirm` | 确认结算单 | FINANCE |
| PUT | `/settlements/{id}/cancel` | 作废结算单 | ADMIN |
| GET | `/fee-rules` | 费率规则列表 | FINANCE |
| POST | `/fee-rules` | 创建费率规则 | ADMIN |
| PUT | `/fee-rules/{id}` | 更新费率规则 | ADMIN |
| PUT | `/fee-rules/{id}/toggle` | 启用/禁用规则 | ADMIN |

### 订单对象 (OrderVO)

```json
{
  "id": 1,
  "orderNo": "ORD-20260614-001",
  "customerId": 1001,
  "customerName": "XX公司",
  "contractNo": "HT-2026-001",
  "totalAmount": 50000.00,
  "calculatedAmount": 49250.00,
  "feeAmount": 750.00,
  "status": 1,
  "remark": "备注",
  "items": [
    {
      "id": 1,
      "productName": "咨询服务",
      "quantity": 1,
      "unitPrice": 30000.00,
      "amount": 30000.00
    }
  ],
  "createTime": "2026-06-14T10:00:00",
  "updateTime": "2026-06-14T10:00:00"
}
```

### 订单状态枚举

| 值 | 含义 |
|----|------|
| 1 | 待结算 |
| 2 | 已结算 |
| 3 | 已关闭 |

### GET `/orders` 分页查询

请求参数：
```
?page=1&size=20&customerName=XX&status=1&startDate=2026-01-01&endDate=2026-06-30&sort=create_time,desc
```

响应：
```json
{
  "code": 200,
  "data": {
    "records": [ {OrderVO} ],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  }
}
```

### GET `/orders/{id}` 订单详情

响应：`{ "code": 200, "data": {OrderVO} }`

### PUT `/orders/{id}` 更新订单

请求：
```json
{
  "customerName": "XX公司（变更）",
  "contractNo": "HT-2026-001-A",
  "totalAmount": 55000.00,
  "remark": "合同变更调整金额",
  "items": [
    {
      "id": 1,
      "productName": "咨询服务",
      "quantity": 1,
      "unitPrice": 35000.00,
      "amount": 35000.00
    }
  ]
}
```

约束：仅允许修改状态=1（待结算）的订单。

### DELETE `/orders/{id}` 删除订单

约束：逻辑删除（status→3 已关闭），仅限 status=1（待结算）。

### PUT `/orders/{id}/status` 更新状态

请求：
```json
{
  "status": 2
}
```

约束：
- 1→2：已结算（需有关联结算单）
- 1→3：已关闭（无关联结算单时可直接关闭）

### GET `/orders/export` Excel 导出

请求参数：`?status=1&startDate=2026-01-01&endDate=2026-06-30&ids=1,2,3`

响应：`Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`，文件流下载。

---

### 结算单对象 (SettlementVO)

```json
{
  "id": 1,
  "settlementNo": "STL-20260614-001",
  "orderId": 1,
  "orderNo": "ORD-20260614-001",
  "customerName": "XX公司",
  "originalAmount": 50000.00,
  "feeDetail": {
    "ruleId": 1,
    "ruleName": "标准合同费率",
    "rate": 0.015,
    "feeAmount": 750.00
  },
  "realAmount": 49250.00,
  "status": 1,
  "remark": "2026年6月结算",
  "createTime": "2026-06-14T10:00:00",
  "updateTime": "2026-06-14T10:00:00"
}
```

### 结算单状态枚举

| 值 | 含义 |
|----|------|
| 1 | 待确认 |
| 2 | 已确认 |
| 3 | 已作废 |

### GET `/settlements` 结算单列表

请求参数：`?page=1&size=20&orderId=1&status=1&startDate=2026-01-01&endDate=2026-06-30`

响应：`{ "code": 200, "data": { "records": [{SettlementVO}], "total": 50, "page": 1, "size": 20 } }`

### GET `/settlements/{id}` 结算单详情

响应：`{ "code": 200, "data": {SettlementVO} }`

### POST `/settlements` 生成结算单

请求：
```json
{
  "orderIds": [1, 2, 3],
  "remark": "2026年6月批量结算"
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "totalCount": 3,
    "successCount": 2,
    "failCount": 1,
    "results": [
      {
        "orderId": 1,
        "settlementNo": "STL-20260614-001",
        "success": true
      },
      {
        "orderId": 3,
        "success": false,
        "errorMsg": "订单已结算"
      }
    ]
  }
}
```

约束：仅能结算 status=1（待结算）且已计算费用的订单。

### PUT `/settlements/{id}/confirm` 确认结算

约束：仅限 status=1（待确认），确认后→2（已确认），关联订单 status→2（已结算）。

### PUT `/settlements/{id}/cancel` 作废结算

约束：仅限 status=1（待确认），作废后→3（已作废），关联订单 status→1（回退待结算）。

---

### 费率规则对象 (FeeRuleVO)

```json
{
  "id": 1,
  "ruleName": "标准合同费率",
  "ruleType": "PERCENTAGE",
  "rate": 0.015,
  "minFee": 100.00,
  "maxFee": 10000.00,
  "conditions": {
    "contractTypes": ["HT"],
    "minAmount": 0
  },
  "priority": 1,
  "status": 1,
  "remark": "适用于所有合同类订单",
  "createTime": "2026-06-14T10:00:00",
  "updateTime": "2026-06-14T10:00:00"
}
```

### 费率规则类型枚举

| 值 | 含义 |
|----|------|
| PERCENTAGE | 百分比费率 |
| FIXED | 固定金额 |
| TIERED | 阶梯费率 |

### 规则状态枚举

| 值 | 含义 |
|----|------|
| 1 | 启用 |
| 0 | 禁用 |

### GET `/fee-rules` 费率规则列表

请求参数：`?page=1&size=20&ruleName=标准&status=1`

响应：`{ "code": 200, "data": { "records": [{FeeRuleVO}], "total": 10, "page": 1, "size": 20 } }`

### POST `/fee-rules` 创建规则

请求：
```json
{
  "ruleName": "VIP客户费率",
  "ruleType": "PERCENTAGE",
  "rate": 0.01,
  "minFee": 50.00,
  "maxFee": 5000.00,
  "conditions": {
    "customerLevels": ["VIP"]
  },
  "priority": 2,
  "remark": "VIP客户优惠费率"
}
```

### PUT `/fee-rules/{id}` 更新规则

请求：同创建，传递全部字段。约束：仅限 status=1 可修改。

### PUT `/fee-rules/{id}/toggle` 启用/禁用

请求：`{"status": 0}` — 仅 `1`（启用）/ `0`（禁用）。

---

### POST `/orders/batch-calc` 批量费用计算

请求：
```json
{
  "orderIds": [1, 2, 3]
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 3,
    "successCount": 2,
    "failCount": 1,
    "results": [
      {
        "orderId": 1,
        "orderNo": "ORD-20260614-001",
        "originalAmount": 10000.00,
        "calculatedAmount": 9850.00,
        "feeDetail": {
          "ruleId": 1,
          "ruleName": "标准合同费率",
          "rate": 0.015,
          "feeAmount": 150.00
        },
        "success": true
      },
      {
        "orderId": 3,
        "orderNo": "ORD-20260614-003",
        "success": false,
        "errorMsg": "未匹配到适用费率规则"
      }
    ]
  }
}
```

### POST `/orders` 创建订单

请求：
```json
{
  "customerId": 1001,
  "customerName": "XX公司",
  "contractNo": "HT-2026-001",
  "totalAmount": 50000.00,
  "items": [
    {
      "productName": "咨询服务",
      "quantity": 1,
      "unitPrice": 30000.00,
      "amount": 30000.00
    },
    {
      "productName": "系统部署",
      "quantity": 1,
      "unitPrice": 20000.00,
      "amount": 20000.00
    }
  ]
}
```

### POST `/orders/import` Excel 导入

请求：`multipart/form-data`，字段 `file`（.xlsx）

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalRows": 100,
    "successRows": 95,
    "failRows": 5,
    "errors": [
      {
        "row": 3,
        "field": "totalAmount",
        "message": "金额格式不正确"
      }
    ]
  }
}
```

---

## 4. 收付款服务 `/api/v1/payments`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/receipts` | 收款记录列表 | FINANCE, ADMIN |
| GET | `/receipts/{id}` | 收款详情 | FINANCE, ADMIN |
| POST | `/receipts` | 创建收款记录 | FINANCE |
| PUT | `/receipts/{id}/confirm` | 到账确认 | FINANCE |
| POST | `/receipts/verify-by-scan` | 扫码核销 | FINANCE |
| GET | `/payments` | 付款记录列表 | FINANCE, ADMIN |
| GET | `/payments/{id}` | 付款详情 | FINANCE, ADMIN |
| POST | `/payments` | 发起付款申请 | FINANCE |
| PUT | `/payments/{id}/approve` | 付款审批 | FINANCE, ADMIN |
| PUT | `/payments/{id}/reject` | 驳回付款 | FINANCE, ADMIN |
| GET | `/bills` | 账单列表 | FINANCE, BIZ |
| GET | `/bills/{id}` | 账单详情 | FINANCE, BIZ |
| POST | `/bills` | 生成账单 | FINANCE |
| POST | `/bills/merge` | 合并账单 | FINANCE |
| POST | `/bills/split` | 拆分账单 | FINANCE |
| POST | `/bills/{id}/send` | 发送账单 | FINANCE |
| GET | `/approvals/pending` | 待审批列表 | FINANCE, ADMIN |
| PUT | `/approvals/{id}/process` | 处理审批节点 | FINANCE, ADMIN |

### POST `/payments` 发起付款

请求：
```json
{
  "payeeName": "XX供应商",
  "amount": 50000.00,
  "supplierId": 2001,
  "orderId": 1,
  "remark": "合同款支付"
}
```

### PUT `/approvals/{id}/process` 审批处理

请求：
```json
{
  "action": "APPROVE",
  "comment": "同意付款"
}
```

`action` 枚举：`APPROVE` / `REJECT` / `TRANSFER`（转审）

---

## 5. 资金流水服务 `/api/v1/funds`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/accounts` | 银行账户列表 | FINANCE, ADMIN |
| GET | `/accounts/{id}` | 账户详情 | FINANCE, ADMIN |
| POST | `/accounts` | 创建银行账户 | ADMIN |
| PUT | `/accounts/{id}` | 更新账户信息 | ADMIN |
| GET | `/transactions` | 流水列表 | FINANCE, ADMIN |
| GET | `/transactions/{id}` | 流水详情 | FINANCE, ADMIN |
| POST | `/transactions` | 手动录入流水 | FINANCE |
| POST | `/transactions/batch-sync` | 银行接口批量同步 | ADMIN |
| GET | `/reconciliations` | 对账记录列表 | FINANCE, ADMIN |
| POST | `/reconciliations/run` | 执行自动对账 | FINANCE |
| GET | `/reconciliations/diffs` | 查看差异记录 | FINANCE |
| PUT | `/reconciliations/{id}/handle` | 处理差异记录 | FINANCE |

#### POST `/reconciliations/run` 自动对账

请求：
```json
{
  "accountId": 1,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
```

响应：
```json
{
  "code": 200,
  "data": {
    "totalMatched": 150,
    "totalDiffs": 5,
    "diffs": [
      {
        "type": "AMOUNT_MISMATCH",
        "systemAmount": 1000.00,
        "actualAmount": 1000.50,
        "diffAmount": 0.50
      }
    ]
  }
}
```

---

## 6. 报表服务 `/api/v1/reports`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/income-expense` | 收支明细报表 | FINANCE, ADMIN |
| GET | `/balance` | 资金余额报表 | FINANCE, ADMIN |
| GET | `/settlement-stats` | 结算统计报表 | FINANCE, ADMIN |
| POST | `/export` | 报表导出（Excel/PDF） | FINANCE, ADMIN |

### POST `/export` 报表导出

请求：
```json
{
  "reportType": "INCOME_EXPENSE",
  "startDate": "2026-01-01",
  "endDate": "2026-06-30",
  "accountIds": [1, 2],
  "exportFormat": "EXCEL"
}
```

`reportType` 枚举：`INCOME_EXPENSE` / `BALANCE` / `SETTLEMENT_STATS`
`exportFormat` 枚举：`EXCEL` / `PDF`

响应：
```json
{
  "code": 200,
  "data": {
    "taskId": "uuid-task-xxx",
    "status": "PROCESSING"
  }
}
```

轮询任务状态：`GET /api/v1/reports/export/status?taskId=xxx`

---

## 7. 通用枚举

### 订单状态 (OrderStatus)

| 值 | 说明 |
|----|------|
| 1 | 待结算 |
| 2 | 已结算 |
| 3 | 已关闭 |

### 收款状态 (ReceiptStatus)

| 值 | 说明 |
|----|------|
| 1 | 待确认 |
| 2 | 已到账 |
| 3 | 已核销 |

### 付款状态 (PaymentStatus)

| 值 | 说明 |
|----|------|
| 1 | 待审批 |
| 2 | 审批中 |
| 3 | 已付款 |
| 4 | 已驳回 |

### 审批动作 (ApprovalAction)

| 值 | 说明 |
|----|------|
| APPROVE | 通过 |
| REJECT | 驳回 |
| TRANSFER | 转审 |

### 对账差异类型 (DiffType)

| 值 | 说明 |
|----|------|
| AMOUNT_MISMATCH | 金额不符 |
| SYSTEM_ONLY | 系统独有 |
| BANK_ONLY | 银行独有 |

---

## 8. 契约变更流程

1. 后端或前端提出变更需求
2. 更新本文档并双方确认
3. 同步更新 Knife4j 注解（开发时自动反映到 Swagger UI）
4. 提交变更到 Git 并注明 breaking change
