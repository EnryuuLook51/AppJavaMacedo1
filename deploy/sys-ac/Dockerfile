FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --chown=10001:10001 acceso-1.0.0.jar /app/app.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
