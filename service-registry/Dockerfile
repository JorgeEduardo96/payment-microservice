# Expects the jar to already be built beforehand (e.g. `./gradlew :service-registry:build -x test`).
# This avoids recompiling/re-resolving dependencies a second time inside Docker on every image build.
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
