# Build stage
FROM maven:3.9-eclipse-temurin-25 AS build

COPY src /home/app/src
COPY pom.xml /home/app

# Build and list the target directory to verify JAR creation
RUN mvn -f /home/app/pom.xml clean package -DskipTests=true && ls -la /home/app/target/

# Package stage
FROM eclipse-temurin:25-jre

COPY --from=build /home/app/target/buddy-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
