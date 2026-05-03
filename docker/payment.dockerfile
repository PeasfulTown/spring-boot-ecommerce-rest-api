FROM eclipse-temurin:17-jdk
RUN mkdir /opt/app
COPY ./payment-service /opt/app
WORKDIR /opt/app
RUN ./mvnw package -Dmaven.test.skip
RUN cp target/*.jar ./app.jar
CMD [ "java", "-jar", "app.jar" ]

