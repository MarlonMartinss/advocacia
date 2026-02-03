# =============================================
# Stage 1: Development
# =============================================
FROM node:20-alpine AS development

WORKDIR /app

# Copiar package.json
COPY package.json ./

# Instalar dependências
RUN npm install

# Copiar código fonte
COPY . .

# Expor porta do Angular CLI
EXPOSE 4200

# Comando para desenvolvimento com hot reload
CMD ["npm", "run", "start:docker"]

# =============================================
# Stage 2: Build para produção
# =============================================
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json ./
RUN npm install

COPY . .
RUN npm run build:prod

# =============================================
# Stage 3: Nginx para produção
# =============================================
FROM nginx:alpine AS production

# Remover config padrão do nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiar build do Angular
COPY --from=builder /app/dist/advocacia-frontend/browser /usr/share/nginx/html

# Copiar configuração customizada do nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expor porta 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
