FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/deeppoemsinc-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 7072

ENTRYPOINT ["java", "-jar", "app.jar"]
