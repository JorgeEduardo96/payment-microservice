# Expects the jar to already be built beforehand (e.g. `./gradlew :notification-service:build -x test -x integrationTest`).
# This avoids recompiling/re-resolving dependencies a second time inside Docker on every image build.
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
