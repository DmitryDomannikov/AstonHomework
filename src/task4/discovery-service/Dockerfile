# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# скачиваем зависимости отдельно для кеша
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# копируем fat-jar
COPY --from=build /app/target/*.jar app.jar
# health env (опционально)
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8761
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
