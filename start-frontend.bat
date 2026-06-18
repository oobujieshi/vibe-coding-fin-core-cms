@echo off
chcp 65001 >nul
echo.
echo ========================================
echo   FinCoreCms 前端 - Vue3
echo   http://localhost:5173
echo ========================================
echo.
cd /d "%~dp0fin-core-web"
call npm run dev
pause
