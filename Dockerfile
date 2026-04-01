FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:resolve -B
COPY src ./src
RUN mvn clean package -DskipTests -B

RUN java -Djarmode=layertools -jar target/*.jar extract

FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

EXPOSE 8000

ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "org.springframework.boot.loader.launch.JarLauncher"]