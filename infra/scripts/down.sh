#!/bin/bash
# =============================================
# Script para parar todos os serviços
# =============================================

set -e

echo "🛑 Parando serviços..."

docker-compose down

echo ""
echo "✅ Serviços parados com sucesso!"
