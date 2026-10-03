FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
COPY mvnw ./mvnw
COPY .mvn ./.mvn
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 auditoriums \
    && useradd --uid 10001 --gid auditoriums --no-create-home auditoriums \
    && mkdir -p /app/photos && chown -R auditoriums:auditoriums /app
WORKDIR /app
COPY --from=build /build/target/auditoriums-0.0.1-SNAPSHOT.jar application.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
