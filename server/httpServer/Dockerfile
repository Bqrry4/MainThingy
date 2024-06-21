FROM openjdk:17

WORKDIR usr/src/app

COPY target/httpServer-0.0.1-SNAPSHOT.jar source.jar
ENTRYPOINT ["java", "-jar", "source.jar"]
