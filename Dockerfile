FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/backend-tech-test-*.jar app.jar
EXPOSE 5000
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
