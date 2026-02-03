@echo off
REM =============================================
REM Script para iniciar todos os serviços (Windows)
REM =============================================

echo 🚀 Iniciando serviços...

REM Verifica se o arquivo .env existe
if not exist .env (
    echo 📄 Criando arquivo .env a partir de .env.example...
    copy .env.example .env
)

REM Inicia os serviços
docker-compose up -d

echo.
echo ✅ Serviços iniciados com sucesso!
echo.
echo 📍 URLs disponíveis:
echo    Backend:  http://localhost:8080
echo    Frontend: http://localhost:4200
echo    Health:   http://localhost:8080/actuator/health
echo    API:      http://localhost:8080/api/tasks
echo.
echo 📋 Comandos úteis:
echo    docker-compose logs -f     # Ver logs
echo    docker-compose down        # Parar serviços
