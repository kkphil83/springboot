FROM java:8-jre
#Get APP home directory using --build-arg
#ARG APP_HOME=.
ENV VERTICLE_FILE springboot-service.jar
# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

# Add an docker user, make work dir
RUN adduser --disabled-password --gecos "" docker &&  echo "docker ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers && mkdir -p $VERTICLE_HOME && chown docker:docker $VERTICLE_HOME

EXPOSE 80

WORKDIR $VERTICLE_HOME

# Copy your fat jar to the container
#COPY ${APP_HOME}/springboot-service6/target/$VERTICLE_FILE $VERTICLE_HOME

# __custom__ innerproject
COPY $VERTICLE_FILE $VERTICLE_HOME
#COPY ./springboot-service/target/$VERTICLE_FILE $VERTICLE_HOME

# Launch the verticle
ENTRYPOINT ["sh", "-c"]

# RUN as docker user
USER docker
CMD ["exec java -jar $VERTICLE_FILE"]
