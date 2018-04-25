FROM maven:3-jdk-8

ENV MAVEN_OPTS "-Xmx1024m"

COPY . /usr/src
WORKDIR /usr/src
RUN mvn clean test package

# Use an official java 7 as a parent image
# FROM ubuntu:latest
FROM tomcat:7-jre8

MAINTAINER William Petit <w.petit@ed.ac.uk>

ARG LOCAL_DATAVAULT_DIR="./datavault"

ENV DATAVAULT_HOME "/docker_datavault-home"

# Install MySql client
RUN apt-get install -y mysql-client

RUN curl -sLo /usr/local/bin/ep https://github.com/kreuzwerker/envplate/releases/download/v0.0.8/ep-linux && chmod +x /usr/local/bin/ep

COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/lib ${DATAVAULT_HOME}/lib
COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/webapps ${DATAVAULT_HOME}/webapps
COPY docker/config ${DATAVAULT_HOME}/config
COPY docker/scripts ${DATAVAULT_HOME}/scripts

# RUN sed -i 's/appBase=\"webapps\"/appBase=\"\/vagrant_datavault-home\/webapps\"/' /usr/local/tomcat/conf/server.xml
RUN ln -s $DATAVAULT_HOME/webapps/datavault-broker /usr/local/tomcat/webapps/datavault-broker

WORKDIR /usr/local/tomcat

# Make port 80 available to the world outside this container
EXPOSE 8080

ENTRYPOINT ["/docker_datavault-home/scripts/docker-entrypoint.sh"]
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
