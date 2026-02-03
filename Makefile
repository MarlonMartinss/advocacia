# =============================================
# Advocacia - Makefile
# =============================================

.PHONY: help up down logs restart clean reset-db \
        backend-local frontend-local \
        build build-backend build-frontend \
        postgres-only test

# Cores para output
YELLOW := \033[1;33m
GREEN := \033[1;32m
NC := \033[0m # No Color

help: ## Mostra esta ajuda
	@echo "$(GREEN)Comandos disponíveis:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'

# =============================================
# Docker Compose - Ambiente completo
# =============================================

up: ## Inicia todos os serviços (postgres + backend + frontend)
	docker-compose up -d
	@echo "$(GREEN)Serviços iniciados!$(NC)"
	@echo "  Backend:  http://localhost:8080"
	@echo "  Frontend: http://localhost:4200"
	@echo "  Health:   http://localhost:8080/actuator/health"

down: ## Para todos os serviços
	docker-compose down
	@echo "$(GREEN)Serviços parados!$(NC)"

logs: ## Mostra logs de todos os serviços
	docker-compose logs -f

logs-backend: ## Mostra logs apenas do backend
	docker-compose logs -f backend

logs-frontend: ## Mostra logs apenas do frontend
	docker-compose logs -f frontend

logs-postgres: ## Mostra logs apenas do postgres
	docker-compose logs -f postgres

restart: down up ## Reinicia todos os serviços

# =============================================
# Desenvolvimento Local
# =============================================

postgres-only: ## Inicia apenas o PostgreSQL (para dev local)
	docker-compose up -d postgres
	@echo "$(GREEN)PostgreSQL iniciado na porta 5432$(NC)"

backend-local: postgres-only ## Roda backend local (requer Java 21)
	cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

frontend-local: ## Roda frontend local (requer Node.js)
	cd frontend && npm install && npm start

# =============================================
# Build
# =============================================

build: ## Builda todas as imagens Docker
	docker-compose build

build-backend: ## Builda apenas a imagem do backend
	docker-compose build backend

build-frontend: ## Builda apenas a imagem do frontend
	docker-compose build frontend

# =============================================
# Limpeza e Reset
# =============================================

clean: down ## Para serviços e remove volumes
	docker-compose down -v
	@echo "$(GREEN)Serviços parados e volumes removidos!$(NC)"

reset-db: ## Reseta o banco de dados (remove volume e recria)
	docker-compose stop postgres
	docker-compose rm -f postgres
	docker volume rm advocacia_postgres_data 2>/dev/null || true
	docker-compose up -d postgres
	@echo "$(GREEN)Banco de dados resetado!$(NC)"

prune: ## Remove imagens, containers e volumes não utilizados
	docker system prune -f
	docker volume prune -f
	@echo "$(GREEN)Sistema Docker limpo!$(NC)"

# =============================================
# Testes
# =============================================

test-backend: ## Roda testes do backend
	cd backend && ./mvnw test

test-frontend: ## Roda testes do frontend
	cd frontend && npm test

# =============================================
# Utilitários
# =============================================

shell-postgres: ## Acessa shell do PostgreSQL
	docker-compose exec postgres psql -U advocacia -d advocacia

shell-backend: ## Acessa shell do container backend
	docker-compose exec backend sh

status: ## Mostra status dos containers
	docker-compose ps
