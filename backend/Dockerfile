# ---- Giai doan 1: bien dich ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom truoc de Docker cache lop tai thu vien, chi tai lai khi pom doi
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Giai doan 2: chay ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Khong chay bang root
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
RUN chown app:app app.jar
USER app

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
