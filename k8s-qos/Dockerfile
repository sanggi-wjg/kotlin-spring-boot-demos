FROM amazoncorretto:21-al2023

WORKDIR /app
COPY build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
