# 开发过程记录

> FinCoreCms 财务管理模块开发过程全记录

---

## 2026-06-14 14:43 — 创建守护规则 (Rule)

### 背景

项目需要三条开发守护规则来确保执行安全和流程可控。最初以 `skill` 形式创建，经讨论后确认这些行为约束更适合用 `rule` 机制实现（始终生效，无需触发条件）。

### 执行内容

将 `fincore-dev-guard` skill 拆分为 3 个独立 Rule：

| Rule | 文件 | 说明 |
|------|------|------|
| 🚫 禁止擅自删除 | `.codebuddy/rules/no-delete.md` | 任何文件删除操作须经用户明确确认 |
| 🛡️ Phase 执行关卡 | `.codebuddy/rules/phase-guard.md` | 每个 Phase 等待用户指令，不自动推进 |
| 📝 Phase 交互日志 | `.codebuddy/rules/phase-log.md` | Phase 执行时记录日志到 `docs/phase-logs/` |

同时更新了 `.codebuddy/skills/fincore-dev-guard/SKILL.md`，使其指向新的 Rule 文件。

### 产出

- `.codebuddy/rules/no-delete.md` — 禁止删除规则
- `.codebuddy/rules/phase-guard.md` — Phase 关卡规则
- `.codebuddy/rules/phase-log.md` — 交互日志规则
- 更新 `.codebuddy/skills/fincore-dev-guard/SKILL.md`

### 关键决策

- Rule vs Skill：行为约束/禁令 → Rule；带流程/工具的任务 → Skill
- 三条规则都是始终生效的行为约束，更适合 Rule 机制

---

## 2026-06-14 14:56 — Linear 项目与任务同步集成

### 背景

需要将执行计划同步到 Linear 中进行可视化管理，并在每个 Phase 执行前后自动更新任务状态。

### 历史（已废弃）

> ~~第一版使用 PowerShell 创建，因编码问题导致中文乱码 + 标题变为英文。已通过 Python 脚本重建。~~

### 最终结果（15:01 重建）

**Linear 项目**：https://linear.app/oobujieshi/project/fincorecms-e016ed362d0e

| Phase | Linear ID | Issue ID | URL |
|-------|-----------|----------|-----|
| Phase 0：开发环境安装 | ALI-11 | `a4180860` | [查看](https://linear.app/oobujieshi/issue/ALI-11) |
| Phase 1：项目初始化 | ALI-12 | `fb8fe740` | [查看](https://linear.app/oobujieshi/issue/ALI-12) |
| Phase 2：基础设施搭建 | ALI-13 | `63282d2f` | [查看](https://linear.app/oobujieshi/issue/ALI-13) |
| Phase 3：订单结算服务 | ALI-14 | `3d41161b` | [查看](https://linear.app/oobujieshi/issue/ALI-14) |
| Phase 4：收付款服务 | ALI-15 | `d3312361` | [查看](https://linear.app/oobujieshi/issue/ALI-15) |
| Phase 5：资金流水与报表 | ALI-16 | `c3d9565b` | [查看](https://linear.app/oobujieshi/issue/ALI-16) |
| Phase 6：小程序端 | ALI-17 | `2e5967bb` | [查看](https://linear.app/oobujieshi/issue/ALI-17) |
| Phase 7：集成测试与部署 | ALI-18 | `e6f87a75` | [查看](https://linear.app/oobujieshi/issue/ALI-18) |
| Phase 8：上线与验收 | ALI-19 | `03bc9add` | [查看](https://linear.app/oobujieshi/issue/ALI-19) |

### 产出

- Linear 项目：FinCoreCms（含 9 个中文 Issue）
- `.codebuddy/rules/linear-sync.md` — Linear 任务同步规则
- `scripts/rebuild-linear.py` — Python 重建脚本（UTF-8 编码，解决乱码）
- `scripts/create-linear-issues.ps1` — PowerShell 版（已废弃，编码问题）
- `scripts/create-linear-subtasks.py` — 子任务批量创建脚本

---

## 2026-06-14 15:10 — 补充创建子任务

### 背景

Phase 级 Issue 已创建，但每个 Phase 下的子任务（T{Phase}-{序号}）未创建。

### 执行内容

为 9 个 Phase Issue 批量创建 66 个子任务（child issues）：

| Phase | 子任务数 | 示例 |
|-------|---------|------|
| Phase 0 | 5 | T0-1 ~ T0-5 |
| Phase 1 | 5 | T1-1 ~ T1-5 |
| Phase 2 | 11 | T2-1 ~ T2-11 |
| Phase 3 | 8 | T3-1 ~ T3-8 |
| Phase 4 | 9 | T4-1 ~ T4-9 |
| Phase 5 | 12 | T5-1 ~ T5-12 |
| Phase 6 | 6 | T6-1 ~ T6-6 |
| Phase 7 | 7 | T7-1 ~ T7-7 |
| Phase 8 | 3 | T8-1 ~ T8-3 |
| **合计** | **66** | |

### 产出

- `scripts/create-linear-subtasks.py` — 子任务创建脚本

### 教训

- PowerShell 的 `ConvertTo-Json` 对中文 GraphQL 查询编码支持差，改用 Python 的 `json.dumps(ensure_ascii=False)` + `utf-8` 编码解决

### 当前规则清单

| # | 规则 | 文件 |
|---|------|------|
| 1 | 🚫 禁止擅自删除 | `.codebuddy/rules/no-delete.md` |
| 2 | 🛡️ Phase 执行关卡 | `.codebuddy/rules/phase-guard.md` |
| 3 | 📝 Phase 交互日志 | `.codebuddy/rules/phase-log.md` |
| 4 | 🔄 Linear 任务同步 | `.codebuddy/rules/linear-sync.md` |

---

## 开发进度

| Phase | 状态 | Linear | 开始时间 | 完成时间 |
|-------|------|--------|----------|----------|
| Phase 0 | ✅ 已完成 | [ALI-11](https://linear.app/oobujieshi/issue/ALI-11) | 2026-06-14 15:20 | 2026-06-14 15:26 |
| Phase 1 | ✅ 已完成 | [ALI-12](https://linear.app/oobujieshi/issue/ALI-12) | 2026-06-14 15:45 | 2026-06-14 16:00 |
| Phase 2 | ✅ 已完成 | [ALI-13](https://linear.app/oobujieshi/issue/ALI-13) | 2026-06-14 20:28 | 2026-06-14 20:38 |
| Phase 3 | ✅ 已完成 | [ALI-14](https://linear.app/oobujieshi/issue/ALI-14) | 2026-06-15 22:00 | 2026-06-15 22:10 |
| Phase 4 | ✅ 已完成 | [ALI-15](https://linear.app/oobujieshi/issue/ALI-15) | 2026-06-16 20:31 | 2026-06-16 20:53 |
| Phase 5 | ✅ 已完成 | [ALI-16](https://linear.app/oobujieshi/issue/ALI-16) | 2026-06-16 22:45 | 2026-06-16 22:55 |
| Phase 6 | ✅ 已完成 | [ALI-17](https://linear.app/oobujieshi/issue/ALI-17) | 2026-06-17 21:24 | 2026-06-17 21:28 |
| Phase 7 | ✅ 已完成 | [ALI-18](https://linear.app/oobujieshi/issue/ALI-18) | 2026-06-17 22:30 | 2026-06-17 22:35 |
| Phase 8 | ✅ 已完成 | [ALI-19](https://linear.app/oobujieshi/issue/ALI-19) | 2026-06-17 23:27 | 2026-06-17 23:30 |
