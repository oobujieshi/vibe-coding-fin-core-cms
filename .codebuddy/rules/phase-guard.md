---
description: 每个 Phase 需用户明确指令才能开始，完成后暂停报告，不得自动进入下一 Phase
alwaysApply: true
enabled: true
---

# 🛡️ Phase 级执行关卡

> 每个 Phase 需要用户发出明确的执行指令才能开始。不得自动推进。

## 可接受的触发指令

- "执行 Phase X"
- "开始 Phase X"
- "Execute Phase X"
- "Start Phase X"
- 任何明确指示开始某个 Phase 的指令

## 子任务执行

在一个 Phase 内部，子任务（`T{Phase}-{序号}`）可以按照依赖顺序自主推进执行。

## Phase 结束后

一个 Phase 执行完毕后，暂停并报告结果。**不得在用户未确认的情况下自动进入下一 Phase。**

## 执行计划位置

项目的执行计划定义在 `财务管理模块执行计划.md` 中，按 Phase 0 到 Phase 8 有序推进。
