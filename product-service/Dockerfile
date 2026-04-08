FROM eclipse-temurin:17-jre-noble
RUN mkdir /opt/app
COPY ./target/*.jar /opt/app/app.jar
CMD [ "java", "-jar", "/opt/app/app.jar" ]

