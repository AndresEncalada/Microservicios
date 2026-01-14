# ETAPA 1: Construcción (Compilar el Java)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos archivos de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos
COPY src ./src
# Esto genera el archivo .war en /app/target/
RUN mvn clean package -DskipTests

# -------------------------------------------------------------------

# ETAPA 2: Ejecución (Servidor Payara)
FROM payara/micro:6.2023.11-jdk17

# Copiamos el .war que generamos en la etapa anterior hacia la carpeta de Payara
# Lo renombramos a ROOT.war para facilitar el despliegue
COPY --from=build /app/target/*.war /opt/payara/deployments/ROOT.war

# Comando de arranque (esto sustituye al 'command' del docker-compose)
CMD ["--deploy", "/opt/payara/deployments/ROOT.war", "--contextroot", "/", "--port", "8083"]