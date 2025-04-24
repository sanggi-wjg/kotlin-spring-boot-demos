FROM amazoncorretto:21.0.7 AS builder

WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

###################################

FROM amazoncorretto:21.0.7

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
