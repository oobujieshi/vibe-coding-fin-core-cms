---
description: 禁止未经用户确认删除任何文件，必须列出影响范围并获明确同意
alwaysApply: true
enabled: true
---

# 🚫 禁止擅自删除文件

> 绝对禁止在未经用户明确确认的情况下执行任何文件或目录的删除操作。

## 禁止的操作

- Shell 命令：`rm`、`rmdir`、`del`、`Remove-Item`、`rd`、`erase`
- Python 代码：`os.remove()`、`os.unlink()`、`shutil.rmtree()`、`pathlib.Path.unlink()`
- 任何会导致文件或目录被移除的操作

## 正确流程

当需要删除文件时，必须：

1. 列出所有将受影响的文件/目录
2. 解释为什么需要删除
3. 向用户展示明确的"确认/取消"提示
4. **仅在收到用户明确同意后才执行**

## 例外

同一会话中创建的临时文件，且用户已确认为安全可删除的。
