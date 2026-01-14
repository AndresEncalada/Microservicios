# ETAPA 1: Construcción (Usamos Maven con Java 21)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1. Copiamos pom.xml y descargamos dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copiamos código y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------------------------------------------------------

# ETAPA 2: Ejecución (Usamos Java 21 ligero)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copiamos el .jar generado
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]