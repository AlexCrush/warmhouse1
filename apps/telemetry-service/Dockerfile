FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/telemetry-service-1.0.0.jar app.jar
EXPOSE 8083
ENV PORT=8083
CMD ["java", "-jar", "app.jar"]
