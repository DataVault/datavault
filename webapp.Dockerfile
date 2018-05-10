FROM maven:3-jdk-8

ENV MAVEN_OPTS "-Xmx1024m"

# By default this is empty, but if you've built the package locally (without Docker) you can speed up repeated builds by copying your ~/.m2/repository into docker/m2/repository
# Any dependencies you don't have will still be downloaded as normal
COPY docker/m2/repository /root/.m2/repository

# Copy just the pom files to cache the dependencies
COPY datavault-assembly/pom.xml /tmp/datavault-assembly/pom.xml
COPY datavault-broker/pom.xml /tmp/datavault-broker/pom.xml
COPY datavault-common/pom.xml /tmp/datavault-common/pom.xml
COPY datavault-webapp/pom.xml /tmp/datavault-webapp/pom.xml
COPY datavault-worker/pom.xml /tmp/datavault-worker/pom.xml
COPY pom.xml /tmp
WORKDIR /tmp
RUN mvn dependency:go-offline --fail-never
# The dependency:go-offline gets a lot of the dependencies, but not all. This would get more, but not sure about other implications
#RUN mvn -s /usr/share/maven/ref/settings-docker.xml install --fail-never

COPY . /usr/src
WORKDIR /usr/src
RUN mvn clean test package

RUN curl -sLo /usr/local/bin/ep https://github.com/kreuzwerker/envplate/releases/download/v0.0.8/ep-linux && chmod +x /usr/local/bin/ep
RUN curl -sLo /usr/local/bin/wait-for-it https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && chmod +x /usr/local/bin/wait-for-it

FROM tomcat:7-jre8-alpine

MAINTAINER William Petit <w.petit@ed.ac.uk>

ARG LOCAL_DATAVAULT_DIR="./datavault"

ENV DATAVAULT_HOME "/docker_datavault-home"

RUN apk add --no-cache su-exec

COPY --from=0 /usr/local/bin/ep /usr/local/bin/ep
COPY --from=0 /usr/local/bin/wait-for-it /usr/local/bin/wait-for-it
COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/lib ${DATAVAULT_HOME}/lib
COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/webapps ${DATAVAULT_HOME}/webapps
COPY docker/config ${DATAVAULT_HOME}/config
COPY docker/scripts ${DATAVAULT_HOME}/scripts

RUN sed -i '/wait-for-it.*RABBITMQ/d' ${DATAVAULT_HOME}/scripts/docker-entrypoint.sh
RUN sed -i '/wait-for-it.*MYSQL/d' ${DATAVAULT_HOME}/scripts/docker-entrypoint.sh

RUN ln -s ${DATAVAULT_HOME}/webapps/datavault-webapp ${CATALINA_HOME}/webapps/datavault-webapp

RUN adduser -D datavault
RUN chown -R datavault:datavault ${DATAVAULT_HOME}
RUN chown -R datavault:datavault ${CATALINA_HOME}

WORKDIR ${CATALINA_HOME}
EXPOSE 8080

ENTRYPOINT ["/docker_datavault-home/scripts/docker-entrypoint.sh"]
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
