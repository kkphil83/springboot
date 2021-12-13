FROM docker.io/azul/zulu-openjdk-alpine:15.0.3

ADD target/*.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]