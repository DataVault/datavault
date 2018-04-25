FROM maven:3-jdk-8

ENV MAVEN_OPTS "-Xmx1024m"

COPY . /usr/src
WORKDIR /usr/src
RUN mvn clean test package

# Use an official java 7 as a parent image
# FROM ubuntu:latest
FROM java:8

MAINTAINER William Petit <w.petit@ed.ac.uk>

ENV DATAVAULT_HOME "/docker_datavault-home"

RUN curl -sLo /usr/local/bin/ep https://github.com/kreuzwerker/envplate/releases/download/v0.0.8/ep-linux && chmod +x /usr/local/bin/ep

# Couldn't use the whole directory as volume or datavault.properties gets overwritten
RUN mkdir -p ${DATAVAULT_HOME}/lib
COPY --from=0 /usr/src/datavault-worker/target/datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar ${DATAVAULT_HOME}/lib/datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar
COPY docker/config ${DATAVAULT_HOME}/config
COPY docker/scripts ${DATAVAULT_HOME}/scripts

WORKDIR ${DATAVAULT_HOME}/lib

# Make port 80 available to the world outside this container
EXPOSE 8080

ENTRYPOINT ["/docker_datavault-home/scripts/docker-entrypoint.sh"]
CMD ["java", "-cp", "datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar:./*", "org.datavaultplatform.worker.WorkerInstance"]
