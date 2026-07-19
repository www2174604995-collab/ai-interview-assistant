# ============================================
# Dockerfile for ai-interview-assistant
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV OPENAI_API_KEY=${OPENAI_API_KEY}
ENV OPENAI_BASE_URL=${OPENAI_BASE_URL}

ENTRYPOINT ["java", "-jar", "app.jar"]
