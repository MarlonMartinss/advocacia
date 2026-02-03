# 📚 Documentação da API

## Base URL

- **Desenvolvimento:** `http://localhost:8080/api`
- **Produção:** `https://seu-dominio.com/api`

## Autenticação

> ⚠️ A autenticação ainda não está implementada. Todos os endpoints são públicos.

## Endpoints

### Tasks

#### Listar todas as tarefas

```http
GET /api/tasks
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Implementar autenticação",
    "done": false,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

---

#### Buscar tarefa por ID

```http
GET /api/tasks/{id}
```

**Parâmetros:**
| Nome | Tipo | Descrição |
|------|------|-----------|
| id | Long | ID da tarefa |

**Response (200):**
```json
{
  "id": 1,
  "title": "Implementar autenticação",
  "done": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Response (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Task não encontrada com id: 1",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

#### Criar tarefa

```http
POST /api/tasks
Content-Type: application/json
```

**Body:**
```json
{
  "title": "Nova tarefa",
  "done": false
}
```

**Validações:**
| Campo | Regra |
|-------|-------|
| title | Obrigatório, 3-255 caracteres |
| done | Opcional, default: false |

**Response (201):**
```json
{
  "id": 1,
  "title": "Nova tarefa",
  "done": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Response (400):**
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Erro de validação nos campos enviados",
  "timestamp": "2024-01-15T10:30:00",
  "fieldErrors": {
    "title": "O título é obrigatório"
  }
}
```

---

#### Atualizar tarefa

```http
PUT /api/tasks/{id}
Content-Type: application/json
```

**Body:**
```json
{
  "title": "Tarefa atualizada",
  "done": true
}
```

**Response (200):**
```json
{
  "id": 1,
  "title": "Tarefa atualizada",
  "done": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

---

#### Alternar status (toggle)

```http
PATCH /api/tasks/{id}/toggle
```

Alterna o campo `done` entre `true` e `false`.

**Response (200):**
```json
{
  "id": 1,
  "title": "Implementar autenticação",
  "done": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

---

#### Excluir tarefa

```http
DELETE /api/tasks/{id}
```

**Response (204):** No Content

**Response (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Task não encontrada com id: 1",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Health Check

```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

## Códigos de Status

| Código | Descrição |
|--------|-----------|
| 200 | OK - Requisição bem sucedida |
| 201 | Created - Recurso criado |
| 204 | No Content - Sem conteúdo (delete) |
| 400 | Bad Request - Erro de validação |
| 404 | Not Found - Recurso não encontrado |
| 500 | Internal Server Error - Erro interno |

## Exemplos com cURL

```bash
# Listar tarefas
curl http://localhost:8080/api/tasks

# Criar tarefa
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Minha tarefa", "done": false}'

# Atualizar tarefa
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Tarefa atualizada", "done": true}'

# Toggle done
curl -X PATCH http://localhost:8080/api/tasks/1/toggle

# Excluir tarefa
curl -X DELETE http://localhost:8080/api/tasks/1
```
