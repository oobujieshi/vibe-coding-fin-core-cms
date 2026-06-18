# Linear MCP Server 启动脚本
# 从项目 .env 读取 LINEAR_API_KEY，注入环境变量后启动 linear-mcp-server

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
