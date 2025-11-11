# Schritt 1: Java-Anwendung bauen
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Kopiere alle Dateien
COPY pom.xml .
COPY src ./src
COPY frontend ./frontend
COPY package.json .
COPY tsconfig.json .
COPY vite.config.ts .

# Baue die Anwendung
RUN mvn clean package -Pproduction -DskipTests

# Schritt 2: Nur das fertige JAR in kleines Image packen
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Kopiere das fertige JAR aus Schritt 1
COPY --from=build /app/target/*.jar app.jar

# Port freigeben
EXPOSE 8000

# Starte die Anwendung mit Docker-Profil
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]

