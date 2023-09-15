FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/acs-0.0.1.jar acs-0.0.1.jar
EXPOSE 8080
CMD ["java", "-jar", "acs-0.0.1.jar"]
