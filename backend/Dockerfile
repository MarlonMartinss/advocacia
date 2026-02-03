# =============================================
# Stage 1: Build
# =============================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar Maven Wrapper e pom.xml primeiro para cache de dependências
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dar permissão de execução ao mvnw
RUN chmod +x mvnw

# Download das dependências (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src/ src/

# Build do projeto
RUN ./mvnw clean package -DskipTests -B

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
