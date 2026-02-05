# Como rodar o projeto (CRUD Usuários)

## 1. Backend (obrigatório primeiro)

```bash
cd backend
mvn spring-boot:run
```

Espere aparecer **"Started AdvocaciaApplication"** no terminal (cerca de 30–60 segundos).

- Backend sobe em: **http://localhost:8080**
- Banco: H2 em memória (não precisa de PostgreSQL)
- Usuário admin criado automaticamente: **admin** / **1234**

## 2. Frontend

```bash
cd frontend
npm start
```

Espere **"Compiled successfully"** e abra **http://localhost:4200**.

O proxy do Angular encaminha `/api` para `localhost:8080`, então as chamadas da tela de Usuários funcionam.

## 3. Testar o CRUD de Usuários

1. Acesse **http://localhost:4200**
2. Faça login: **admin** / **1234**
3. No menu, clique em **Usuários**
4. Você deve ver a lista (inicialmente só o admin) e:
   - **+ Novo usuário**: abre o modal para criar
   - **Editar** (ícone de lápis): edita o usuário
   - **Excluir** (ícone de lixeira): pede confirmação e exclui

Se aparecer erro na tela, confira:

- Backend está rodando em 8080?
- Você fez login com admin/1234?
- Reiniciou o frontend (`npm start`) após mudanças no environment?
