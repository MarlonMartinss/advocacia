#!/bin/bash
# =============================================
# Script para resetar o banco de dados
# =============================================

set -e

echo "⚠️  ATENÇÃO: Este comando irá APAGAR todos os dados do banco!"
read -p "Tem certeza que deseja continuar? (y/N): " confirm

if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "❌ Operação cancelada."
    exit 0
fi

echo ""
echo "🗑️  Parando e removendo container do PostgreSQL..."
docker-compose stop postgres
docker-compose rm -f postgres

echo ""
echo "🗑️  Removendo volume de dados..."
docker volume rm advocacia_postgres_data 2>/dev/null || true

echo ""
echo "🚀 Recriando PostgreSQL..."
docker-compose up -d postgres

echo ""
echo "⏳ Aguardando PostgreSQL ficar pronto..."
sleep 5

echo ""
echo "✅ Banco de dados resetado com sucesso!"
echo ""
echo "💡 Dica: Reinicie o backend para aplicar as migrations:"
echo "   docker-compose restart backend"
