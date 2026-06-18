---
description: 每个 Phase 执行前后必须同步 Linear 状态（In Progress/Done）
alwaysApply: true
enabled: true
---

# 🔄 Linear 任务同步

> 每个 Phase 执行前、执行后，必须同步更新 Linear 中对应 Issue 的状态。

## Linear 项目信息

| 项目 | URL |
|------|-----|
| **FinCoreCms** | https://linear.app/oobujieshi/project/fincorecms-e016ed362d0e |

## Phase → Issue 映射

| Phase | Issue ID | Linear ID | URL |
|-------|----------|-----------|-----|
| Phase 0 | `a4180860-1b71-4c30-84bf-c66d0740bde5` | ALI-11 | [查看](https://linear.app/oobujieshi/issue/ALI-11) |
| Phase 1 | `fb8fe740-1b9b-4870-ab76-d491fa3d9f15` | ALI-12 | [查看](https://linear.app/oobujieshi/issue/ALI-12) |
| Phase 2 | `63282d2f-593e-4630-8ccd-f967b8eb6bfd` | ALI-13 | [查看](https://linear.app/oobujieshi/issue/ALI-13) |
| Phase 3 | `3d41161b-5682-48ed-ae16-920c10205606` | ALI-14 | [查看](https://linear.app/oobujieshi/issue/ALI-14) |
| Phase 4 | `d3312361-b8c6-46c4-9a19-9cdbac9fa7dc` | ALI-15 | [查看](https://linear.app/oobujieshi/issue/ALI-15) |
| Phase 5 | `c3d9565b-ab7a-41b7-a88e-dec5dfaa1a9b` | ALI-16 | [查看](https://linear.app/oobujieshi/issue/ALI-16) |
| Phase 6 | `2e5967bb-2a9d-4c94-9fbe-099c54736c38` | ALI-17 | [查看](https://linear.app/oobujieshi/issue/ALI-17) |
| Phase 7 | `e6f87a75-bbb3-49cd-99f6-cfb00ad36c10` | ALI-18 | [查看](https://linear.app/oobujieshi/issue/ALI-18) |
| Phase 8 | `03bc9add-34ad-4a58-bbfe-def4bd153501` | ALI-19 | [查看](https://linear.app/oobujieshi/issue/ALI-19) |

## 同步规则

### Phase 开始执行时

收到用户「执行 Phase X」指令后，在开始实际工作前：

1. 将对应 Linear Issue 状态更新为 **In Progress**
2. 可在 Issue 中追加评论记录开始时间

### Phase 执行完毕后

Phase X 所有子任务完成、向用户报告结果后：

1. 将对应 Linear Issue 状态更新为 **Done**
2. 在 Issue 评论中记录：执行摘要、产出文件列表

### Phase 暂停/阻塞时

如 Phase 中途需要等待用户决策：

1. 将 Linear Issue 更新评论说明阻塞原因
2. 状态可保持不变或手动标记

## 操作方式

通过 Python 脚本 `scripts/rebuild-linear.py` 中的 `gql()` 函数调用 Linear GraphQL API。

API Key 存储在项目 `.env` 的 `LINEAR_API_KEY` 字段中。
