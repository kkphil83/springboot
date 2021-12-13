FROM image-registry.openshift-image-registry.svc:5000/openshift/openjdk-11-rhel8:1.0

ADD target/*.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]