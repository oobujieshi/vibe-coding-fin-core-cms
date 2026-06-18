# MCP 接入指南：Linear

> 接入日期：2026-06-14  
> 适用范围：CodeBuddy IDE + Windows 环境

---

## 1. 最终架构

```
mcp.json                     .env (项目根目录)
┌──────────────────┐         ┌──────────────────────┐
│ command: powershell│────────→│ LINEAR_API_KEY=lin_... │
│ args: -File        │  读取   │ MYSQL_PASSWORD=...     │
│   start-linear-mcp.ps1  │         │ REDIS_PASSWORD=...    │
└──────────────────┘         └──────────────────────┘
        │
        ▼
scripts/start-linear-mcp.ps1
  1. 读取 ../.env
  2. 注入 LINEAR_API_KEY 到进程环境变量
  3. 执行 npx -y linear-mcp-server
```

---

## 2. 接入步骤

### Step 1：获取 Linear API Key

1. 打开 [linear.app](https://linear.app)，登录工作区
2. 左下角头像 → **Settings** → **API** 标签页
3. 点击 **Create new API key**，复制密钥（格式 `lin_api_xxx`）

### Step 2：密钥存入 `.env`

在项目根目录 `.env` 中添加：

```env
LINEAR_API_KEY=lin_api_xxx
```

### Step 3：确定 npm 包名

> ⚠️ **踩坑**：`@modelcontextprotocol/server-linear` — 不存在（npm 404）

正确的包名是：**`linear-mcp-server`**

```bash
npm install -g linear-mcp-server
```

### Step 4：创建启动脚本

文件名：`scripts/start-linear-mcp.ps1`

> ⚠️ **踩坑**：`$PSScriptRoot` 指向脚本自身目录，`.env` 在上级，需用 `..\.env`

```powershell
$envFile = Join-Path $PSScriptRoot "..\.env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and ($line -notmatch '^#') -and ($line -match '^(.+?)=(.+)$')) {
            $key = $matches[1].Trim()
            $val = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $val, "Process")
        }
    }
} else {
    Write-Error "ERROR: .env not found at $envFile"
    exit 1
}

if (-not $env:LINEAR_API_KEY) {
    Write-Error "ERROR: LINEAR_API_KEY not set in .env"
    exit 1
}

Write-Host "LINEAR_API_KEY loaded from .env successfully"
npx -y linear-mcp-server
```

### Step 5：配置 `mcp.json`

路径：`C:/Users/<用户名>/.codebuddy/mcp.json`

> ⚠️ **踩坑**：`mcp.json` 的 `env` 字段只接受**静态字符串值**，不能引用文件或环境变量。必须通过启动脚本间接加载。

```json
{
  "mcpServers": {
    "linear": {
      "command": "powershell",
      "args": ["-ExecutionPolicy", "Bypass", "-File", "d:/workspace/java/vibecoding/FinCoreCms/scripts/start-linear-mcp.ps1"]
    }
  }
}
```

### Step 6：验证

在 CodeBuddy IDE 中：
1. 侧边栏右上角齿轮 ⚙️ → **MCP** 标签页
2. 检查 `linear` 服务状态是否**绿色**
3. 或点击 **Try to Run** 手动验证

---

## 3. 关键经验

| 经验 | 说明 |
|------|------|
| **npm 包名验证** | 配置前先 `npm view <包名> version` 确认存在 |
| **`mcp.json` 的 `env` 限制** | 只支持静态字符串，不支持 `$env:VAR` 或文件引用 |
| **启动脚本模式** | 密钥放 `.env` → 脚本加载 → `mcp.json` 调用脚本 |
| **`$PSScriptRoot` 路径** | 指向脚本所在目录，取上级用 `..` |
| **MCP Server 是常驻进程** | 无法在终端直接验证，需在 IDE MCP 面板查看状态 |
| **⚠️ PowerShell 变量陷阱** | 传参给外部程序时 `$null` 会被展开为空字符串，操作含 `$` 的路径需用转义 |

---

## 4. 通用 MCP 接入模式

后续接入任何 MCP 服务，按此模板：

```
1. 密钥 → .env
2. 写 scripts/start-<service>-mcp.ps1（加载 .env + 启动 server）
3. mcp.json 中用 powershell -File 调用脚本
4. IDE MCP 面板验证
```

### 模板：`scripts/start-xxx-mcp.ps1`

```powershell
$envFile = Join-Path $PSScriptRoot "..\.env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and $line -notmatch '^#' -and $line -match '^(.+?)=(.+)$') {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
        }
    }
}
npx -y <mcp-server-package-name>
```

### 模板：`mcp.json` 条目

```json
"<service-name>": {
  "command": "powershell",
  "args": ["-ExecutionPolicy", "Bypass", "-File", "d:/path/to/scripts/start-xxx-mcp.ps1"]
}
```

---

## 5. 相关文件

| 文件 | 作用 |
|------|------|
| `FinCoreCms/.env` | 所有密钥集中存储 |
| `FinCoreCms/scripts/start-linear-mcp.ps1` | Linear MCP 启动脚本 |
| `C:/Users/asus/.codebuddy/mcp.json` | CodeBuddy MCP 配置 |
