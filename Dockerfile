# ---------- Этап 1: сборка приложения ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Сначала копируем только pom.xml и скачиваем зависимости,
# чтобы Docker закешировал этот слой
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Копируем исходный код и собираем JAR (тесты запускаются отдельно)
COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Этап 2: образ для запуска ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
VOLUME /tmp

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
