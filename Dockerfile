FROM eclipse-temurin:22-jre
WORKDIR /app
COPY target/money-tracker-0.0.1-SNAPSHOT.jar money-tracker.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "money-tracker.jar"]