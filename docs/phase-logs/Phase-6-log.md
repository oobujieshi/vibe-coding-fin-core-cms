# Phase 6：小程序端

- **开始**: 2026-06-17 21:24
- **Linear**: [ALI-17](https://linear.app/oobujieshi/issue/ALI-17)

## AI 执行摘要

Phase 6 全部 6 个子任务完成。小程序 7 个页面（登录首页+6个功能页），共 28 个文件。

---

## 产出文件

| 文件 | 说明 | 状态 |
|------|------|------|
| app.js | 全局登录/请求封装/token管理 | ✅ |
| pages/index/index.* | 登录+功能导航首页 | ✅ |
| pages/bills/bills.* | 账单列表（下拉刷新+加载更多） | ✅ |
| pages/bill-detail/bill-detail.* | 账单详情 | ✅ |
| pages/payment/payment.* | 扫码支付确认 | ✅ |
| pages/scan-verify/scan-verify.* | 扫码核销 | ✅ |
| pages/approvals/approvals.* | 待审批列表+通过/驳回 | ✅ |
| pages/reports/reports.* | 收支/余额/结算三Tab报表 | ✅ |

## Phase 6 完成检查

| 任务 | 状态 |
|------|------|
| T6-1 登录+授权 | ✅ 用户名密码登录+token存储 |
| T6-2 账单查询 | ✅ 列表+详情+下拉刷新 |
| T6-3 支付确认 | ✅ 扫码/手动输入+确认支付 |
| T6-4 扫码核销 | ✅ wx.scanCode+核销码验证 |
| T6-5 审批处理 | ✅ 列表+通过/驳回(带原因) |
| T6-6 报表摘要 | ✅ 收支/余额/结算三Tab卡片 |
| **共计** | **6/6 ✅** |
