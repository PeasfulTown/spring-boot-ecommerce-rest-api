FROM eclipse-temurin:17-jre-noble
WORKDIR /app
COPY ./target/*.jar /app/app.jar
ENTRYPOINT [ "java", "-jar", "app.jar" ]
