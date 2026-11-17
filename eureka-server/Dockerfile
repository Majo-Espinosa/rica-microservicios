# ---------- Etapa 1: compilar ----------
# Imagen con Maven y JDK 25. Aqui se descargan dependencias y se genera el .jar.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
# Primero solo el pom: Docker guarda esta capa en cache y no vuelve a descargar
# las dependencias mientras el pom no cambie.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# Despues el codigo fuente (lo que mas cambia) y la compilacion.
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Etapa 2: ejecutar ----------
# Imagen final: solo el JRE (sin Maven ni JDK ni codigo fuente), mucho mas liviana.
FROM eclipse-temurin:25-jre
WORKDIR /app
# No se ejecuta como root dentro del contenedor.
RUN useradd --system --no-create-home rica
USER rica
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
