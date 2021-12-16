FROM image-registry.openshift-image-registry.svc:5000/openshift/ubi8-openjdk-8:1.3

ADD target/*.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]