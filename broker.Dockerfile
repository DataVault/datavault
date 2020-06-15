FROM datavault/maven-build:latest
# will trigger the maven build

FROM tomcat:7-jre8-alpine

RUN apk add --no-cache tzdata
ENV TZ BST

MAINTAINER William Petit <w.petit@ed.ac.uk>

ARG LOCAL_DATAVAULT_DIR="./datavault"

ENV DATAVAULT_HOME "/docker_datavault-home"

RUN apk add --no-cache mysql curl su-exec

COPY --from=0 /usr/local/bin/ep /usr/local/bin/ep
COPY --from=0 /usr/local/bin/wait-for-it /usr/local/bin/wait-for-it
COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/lib ${DATAVAULT_HOME}/lib
COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/webapps ${DATAVAULT_HOME}/webapps

COPY docker/config ${DATAVAULT_HOME}/config
COPY docker/scripts ${DATAVAULT_HOME}/scripts
COPY docker/pure/ /tmp/

RUN ln -s ${DATAVAULT_HOME}/webapps/datavault-broker ${CATALINA_HOME}/webapps/datavault-broker

RUN adduser -D datavault
RUN chown -R datavault:datavault ${DATAVAULT_HOME}
RUN chown -R datavault:datavault ${CATALINA_HOME}

WORKDIR ${CATALINA_HOME}
EXPOSE 8080

ENTRYPOINT ["/docker_datavault-home/scripts/docker-entrypoint.sh", "broker"]
CMD ["/usr/local/tomcat/bin/catalina.sh", "run"]
