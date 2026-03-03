# ---- build stage ----
FROM maven:3.9-eclipse-temurin-22 AS build
WORKDIR /app

# copy pom first for dependency cache
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

# now copy source and build
COPY src src
RUN ./mvnw -q -DskipTests package

# ---- run stage ----
FROM eclipse-temurin:22-jre
WORKDIR /app

# copy jar from build stage (use wildcard so it doesn't break if version changes)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]