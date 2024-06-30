FROM openjdk:17

WORKDIR usr/src/app

COPY target/coapServer-1.0-SNAPSHOT.jar source.jar
ENTRYPOINT ["java", "-jar", "source.jar"]
