FROM amazoncorretto:17.0.9-al2023

WORKDIR /app
COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-XX:MinRAMPercentage=50.0", "-XX:MaxRAMPercentage=50.0", "-jar", "app.jar"]
