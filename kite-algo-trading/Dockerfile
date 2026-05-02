FROM maven:3.8.6-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/target/kite-algo-trading-1.0.0.jar app.jar

EXPOSE 8080

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/kite_trading
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres

ENTRYPOINT ["java", "-jar", "app.jar"]
