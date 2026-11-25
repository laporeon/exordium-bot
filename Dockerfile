FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -B

FROM eclipse-temurin:21-jre-alpine-3.21
WORKDIR /app

RUN apk upgrade --no-cache \
    && addgroup -S javoidbot \
    && adduser -S javoidbot -G javoidbot

COPY --from=build --chown=javoidbot:javoidbot /app/target/javoidbot.jar app.jar

USER javoidbot

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]