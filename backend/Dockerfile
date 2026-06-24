# syntax=docker/dockerfile:1

# ---- Build stage: compila o jar com Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa dependências primeiro (camada cacheável enquanto o pom não muda)
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Copia o código e empacota. Os testes rodam no CI antes do build da imagem (TDD no pipeline),
# por isso aqui pulamos os testes para um build de imagem mais rápido.
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage: imagem enxuta só com JRE ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuário não-root
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
