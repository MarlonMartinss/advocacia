# 📋 Advocacia - Sistema de Gestão

Sistema fullstack para gestão de tarefas, desenvolvido com **Java 21 + Spring Boot** no backend e **Angular 18** no frontend.

## 📁 Estrutura do Projeto

```
advocacia/
├── backend/                    # Spring Boot (Java 21, Maven)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/advocacia/
│   │   │   │   ├── config/         # Configurações (CORS)
│   │   │   │   ├── controller/     # REST Controllers
│   │   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   ├── entity/         # Entidades JPA
│   │   │   │   ├── exception/      # Exception Handlers
│   │   │   │   ├── repository/     # Repositórios JPA
│   │   │   │   └── service/        # Serviços de negócio
│   │   │   └── resources/
│   │   │       ├── db/migration/   # Migrations Flyway
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   └── test/
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw / mvnw.cmd
├── frontend/                   # Angular 18
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/     # Componentes Angular
│   │   │   ├── models/         # Interfaces TypeScript
│   │   │   └── services/       # Serviços HTTP
│   │   └── environments/       # Configurações por ambiente
│   ├── Dockerfile
│   ├── package.json
│   └── angular.json
├── infra/
│   └── scripts/               # Scripts de automação
├── docs/                      # Documentação
├── docker-compose.yml
├── Makefile
├── .env.example
└── README.md
```

## 🛠️ Pré-requisitos

### Para rodar com Docker (recomendado)
- [Docker](https://docs.docker.com/get-docker/) (versão 20.10+)
- [Docker Compose](https://docs.docker.com/compose/install/) (versão 2.0+)

### Para desenvolvimento local
- **Java 21** - [Download](https://adoptium.net/temurin/releases/?version=21)
- **Node.js 20 LTS** - [Download](https://nodejs.org/)
- **Maven 3.9+** (opcional, o projeto usa Maven Wrapper)

## 🚀 Início Rápido

### 1️⃣ Clone e configure

```bash
# Clone o repositório
git clone <url-do-repositorio>
cd advocacia

# Copie o arquivo de variáveis de ambiente
cp .env.example .env
```

### 2️⃣ Inicie com Docker (1 comando!)

```bash
# Inicia todos os serviços
docker-compose up -d

# Ou use o Makefile
make up
```

### 3️⃣ Acesse

| Serviço | URL |
|---------|-----|
| **Frontend** | http://localhost:4200 |
| **Backend API** | http://localhost:8080/api |
| **Health Check** | http://localhost:8080/actuator/health |
| **Swagger** (futuro) | http://localhost:8080/swagger-ui.html |

## 💻 Opções de Desenvolvimento

### Opção A: Tudo via Docker (mais simples)

```bash
# Inicia todos os serviços containerizados
docker-compose up -d

# Ver logs em tempo real
docker-compose logs -f

# Parar tudo
docker-compose down
```

### Opção B: Desenvolvimento Local (hot reload)

Esta opção oferece melhor experiência de desenvolvimento com hot reload:

```bash
# Terminal 1: Inicia apenas o PostgreSQL
make postgres-only
# ou: docker-compose up -d postgres

# Terminal 2: Backend local (Java 21 necessário)
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 3: Frontend local (Node.js necessário)
cd frontend
npm install
npm start
```

### Opção C: Backend Docker + Frontend Local

```bash
# Inicia PostgreSQL e Backend via Docker
docker-compose up -d postgres backend

# Frontend local para desenvolvimento rápido
cd frontend
npm install
npm start
```

## 📋 Comandos Úteis (Makefile)

```bash
make help           # Lista todos os comandos

# Docker
make up             # Inicia todos os serviços
make down           # Para todos os serviços
make logs           # Mostra logs de todos os serviços
make restart        # Reinicia tudo

# Desenvolvimento
make postgres-only  # Inicia apenas PostgreSQL
make backend-local  # Roda backend localmente
make frontend-local # Roda frontend localmente

# Banco de dados
make reset-db       # Reseta o banco de dados
make shell-postgres # Acessa shell do PostgreSQL

# Build
make build          # Builda todas as imagens
make build-backend  # Builda imagem do backend
make build-frontend # Builda imagem do frontend

# Status
make status         # Mostra status dos containers
```

## 🔌 API REST

### Endpoints `/api/tasks`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/tasks` | Lista todas as tarefas |
| `GET` | `/api/tasks/{id}` | Busca tarefa por ID |
| `POST` | `/api/tasks` | Cria nova tarefa |
| `PUT` | `/api/tasks/{id}` | Atualiza tarefa |
| `PATCH` | `/api/tasks/{id}/toggle` | Alterna status done |
| `DELETE` | `/api/tasks/{id}` | Remove tarefa |

### Exemplos de Payload

#### Criar Tarefa (POST /api/tasks)

**Request:**
```json
{
  "title": "Implementar autenticação",
  "done": false
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Implementar autenticação",
  "done": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Listar Tarefas (GET /api/tasks)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Implementar autenticação",
    "done": false,
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "title": "Configurar CI/CD",
    "done": true,
    "createdAt": "2024-01-14T09:00:00"
  }
]
```

#### Atualizar Tarefa (PUT /api/tasks/1)

**Request:**
```json
{
  "title": "Implementar autenticação JWT",
  "done": true
}
```

#### Alternar Status (PATCH /api/tasks/1/toggle)

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Implementar autenticação JWT",
  "done": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

### Erros de Validação

**Request inválida:**
```json
{
  "title": "ab"
}
```

**Response (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Erro de validação nos campos enviados",
  "timestamp": "2024-01-15T10:30:00",
  "fieldErrors": {
    "title": "O título deve ter entre 3 e 255 caracteres"
  }
}
```

## 🐛 Troubleshooting

### Porta já em uso

```bash
# Verificar o que está usando a porta 8080
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080

# Matar processo (Windows - substitua PID)
taskkill /PID <PID> /F

# Matar processo (Linux/Mac)
kill -9 <PID>
```

### Reset completo do ambiente

```bash
# Para tudo e remove volumes
make clean
# ou: docker-compose down -v

# Remove imagens do projeto
docker rmi advocacia-backend advocacia-frontend

# Inicia do zero
make up
```

### Backend não conecta no PostgreSQL

1. Verifique se o PostgreSQL está rodando:
```bash
docker-compose ps
```

2. Verifique as variáveis de ambiente no `.env`

3. Aguarde o healthcheck do PostgreSQL:
```bash
docker-compose logs postgres
```

### Frontend não conecta no Backend

1. Verifique se o backend está rodando:
```bash
curl http://localhost:8080/actuator/health
```

2. Verifique o CORS no backend (`CorsConfig.java`)

3. Verifique a URL da API no frontend (`environment.ts`)

### Limpar cache do Docker

```bash
# Remove containers, imagens e volumes não utilizados
docker system prune -a --volumes
```

## 🔧 Variáveis de Ambiente

Copie `.env.example` para `.env` e ajuste conforme necessário:

```bash
# Banco de Dados
POSTGRES_DB=advocacia
POSTGRES_USER=advocacia
POSTGRES_PASSWORD=advocacia123

# Spring Boot
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/advocacia
SPRING_DATASOURCE_USERNAME=advocacia
SPRING_DATASOURCE_PASSWORD=advocacia123
```

## 📦 Stack Tecnológica

### Backend
- Java 21
- Spring Boot 3.2
- Spring Data JPA
- Spring Validation
- Spring Actuator
- Flyway (migrations)
- PostgreSQL 16
- Lombok
- Maven

### Frontend
- Angular 18
- TypeScript 5.4
- RxJS
- Angular CLI

### Infraestrutura
- Docker & Docker Compose
- Nginx (produção)

## 📝 Licença

Este projeto está sob a licença MIT.
