FROM alpine:3.23
RUN apk update && apk add openjdk21-jdk && apk add --no-cache maven
COPY . /monitorable-application
RUN mvn -f /monitorable-application/pom.xml package
ENTRYPOINT ["java", "-jar", "/monitorable-application/target/app.jar"]