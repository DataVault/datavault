# Use an official java 7 as a parent image
# FROM ubuntu:latest
FROM tomcat:7-jre8

MAINTAINER William Petit <w.petit@ed.ac.uk>

ARG LOCAL_DATAVAULT_DIR="./datavault"

ENV MAVEN_OPTS "-Xmx1024m"
ENV DATAVAULT_HOME "/docker_datavault-home"

# Update ubuntu
RUN apt-get update && apt-get install -y procps

# Install usefull tools
RUN apt-get -y install vim

# Install MySql client
RUN apt-get install -y mysql-client

# RUN sed -i 's/appBase=\"webapps\"/appBase=\"\/vagrant_datavault-home\/webapps\"/' /usr/local/tomcat/conf/server.xml
RUN ln -s $DATAVAULT_HOME/webapps/datavault-broker /usr/local/tomcat/webapps/datavault-broker
RUN ln -s $DATAVAULT_HOME/webapps/datavault-webapp /usr/local/tomcat/webapps/datavault-webapp

WORKDIR /usr/local/tomcat

# Make port 80 available to the world outside this container
EXPOSE 8080

#CMD supervisord -c /etc/supervisor/supervisord.conf  -n
CMD /docker_datavault-home/scripts/wait-for-rabbitmq.sh;/usr/local/tomcat/bin/catalina.sh run
