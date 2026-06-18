@echo off
chcp 65001 >nul
set "JAVA_HOME=D:\worktools\PyCharm 2025.3.1\jbr"
set "MVN=D:\worktools\apache-maven-3.9.9\bin\mvn.cmd"
set "BASE=%~dp0"

echo.
echo ========================================
echo   FinCoreCms - 一键启动全部服务
echo ========================================
echo.
echo [1/2] 编译项目...
call %MVN% install -DskipTests -q -f "%BASE%pom.xml"
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] 编译失败，请检查错误
    pause & exit /b 1
)
echo [OK] 编译完成

echo.
echo [2/2] 启动服务...
start "Order-8081"    cmd /c "%MVN% spring-boot:run -DskipTests -f %BASE%fin-core-order\pom.xml"
start "Payment-8082"  cmd /c "%MVN% spring-boot:run -DskipTests -f %BASE%fin-core-payment\pom.xml"
start "Fund-8083"     cmd /c "%MVN% spring-boot:run -DskipTests -f %BASE%fin-core-fund\pom.xml"
start "Report-8084"   cmd /c "%MVN% spring-boot:run -DskipTests -f %BASE%fin-core-report\pom.xml"
start "Web-5173"      cmd /c "cd /d %BASE%fin-core-web && npm run dev"

echo.
echo   订单服务: http://localhost:8081
echo   支付服务: http://localhost:8082
echo   资金服务: http://localhost:8083
echo   报表服务: http://localhost:8084
echo   前端页面: http://localhost:5173
echo.
echo   登录账号: admin / admin123
echo.
echo   按任意键停止所有服务...
pause >nul
taskkill /f /im java.exe  >nul 2>&1
taskkill /f /im node.exe  >nul 2>&1
echo 服务已停止
pause
