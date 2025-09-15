# Builder stage: сборка с Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
# Копируем весь проект (parent POM и модули)
COPY . .
# Сборка только gateway-service + зависимые модули (без тестов)
RUN mvn -B -DskipTests -pl gateway-service -am package  # Исправлено

# Runtime stage: JRE для запуска
FROM eclipse-temurin:21-jre
WORKDIR /app
# Копируем собранный JAR (путь от корня build)
COPY --from=builder /build/gateway-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
