# Use an official java 7 as a parent image
# FROM ubuntu:latest
FROM java:8

MAINTAINER William Petit <w.petit@ed.ac.uk>

ENV MAVEN_OPTS "-Xmx1024m"
ENV DATAVAULT_HOME "/docker_datavault-home"

# Update ubuntu
RUN apt-get update && apt-get install -y procps

# Install usefull tools
RUN apt-get -y install vim

# Install supervisor
RUN apt-get install -y supervisor
RUN mkdir -p /var/log/supervisor

ADD docker/supervisor_workers.conf /etc/supervisor/conf.d/workers.conf

# Couldn't use the whole directory as volume or datavault.properties gets overwritten
RUN mkdir -p /docker_datavault-home/{config,lib}
ADD datavault-worker/target/datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar /docker_datavault-home/lib/datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar
# Use customised properties
ADD docker/datavault.properties /docker_datavault-home/config/datavault.properties

WORKDIR /docker_datavault-home

# Make port 80 available to the world outside this container
EXPOSE 8080

CMD supervisord -c /etc/supervisor/supervisord.conf  -n