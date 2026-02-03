@echo off
REM =============================================
REM Script para parar todos os serviços (Windows)
REM =============================================

echo 🛑 Parando serviços...

docker-compose down

echo.
echo ✅ Serviços parados com sucesso!
