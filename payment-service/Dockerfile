FROM eclipse-temurin:25-jre-alpine
COPY target/*.jar app.jar
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]