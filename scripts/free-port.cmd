@echo off
REM 一键释放端口（默认 8080）。双击直接运行，或：free-port.cmd 9090 指定端口。
setlocal
set "PORT=%~1"
if "%PORT%"=="" set "PORT=8080"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0free-port.ps1" -Port %PORT%
if /I not "%~2"=="nopause" (
  echo.
  pause
)
