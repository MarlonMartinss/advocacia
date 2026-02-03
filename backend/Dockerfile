# =============================================
# Stage 1: Build
# =============================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar pom.xml primeiro para cache de dependências
COPY pom.xml ./

# Download das dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src/ src/

# Build do projeto
RUN mvn clean package -DskipTests -B

# =============================================
# Stage 2: Runtime
# =============================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Criar usuário não-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Mudar para usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Executar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
