# Build stage
FROM gradle:8.11.1-jdk17 AS build
WORKDIR /workspace

COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle src src
RUN gradle clean bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --uid 1001 --create-home --shell /usr/sbin/nologin appuser
COPY --from=build --chown=appuser:appuser /workspace/build/libs/*.jar /app/store-application.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
USER appuser
ENTRYPOINT ["java", "-jar", "/app/store-application.jar"]
