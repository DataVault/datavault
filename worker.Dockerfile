FROM datavault/maven-build:latest
# will trigger the maven build

FROM openjdk:8-jre-alpine

MAINTAINER William Petit <w.petit@ed.ac.uk>

ENV DATAVAULT_HOME "/docker_datavault-home"

RUN apk add --no-cache bash curl su-exec

RUN mkdir -p ${DATAVAULT_HOME}/lib

COPY --from=0 /usr/local/bin/ep /usr/local/bin/ep
COPY --from=0 /usr/local/bin/wait-for-it /usr/local/bin/wait-for-it
COPY --from=0 /usr/src/datavault-worker/target/datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar ${DATAVAULT_HOME}/lib/datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar

COPY docker/config ${DATAVAULT_HOME}/config
COPY docker/scripts ${DATAVAULT_HOME}/scripts

RUN adduser -D datavault
RUN chown -R datavault:datavault ${DATAVAULT_HOME}

WORKDIR ${DATAVAULT_HOME}/lib

ENTRYPOINT ["/docker_datavault-home/scripts/docker-entrypoint.sh", "worker"]
CMD ["java", "-cp", "datavault-worker-1.0-SNAPSHOT-jar-with-dependencies-spring.jar:./*", "org.datavaultplatform.worker.WorkerInstance"]
