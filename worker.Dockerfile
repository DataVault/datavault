FROM datavault/maven-build:latest
# will trigger the maven build

FROM openjdk:8-jre-alpine

RUN apk add --no-cache tzdata
ENV TZ BST

MAINTAINER William Petit <w.petit@ed.ac.uk>

ENV DATAVAULT_HOME "/docker_datavault-home"

RUN apk add --no-cache bash curl su-exec

RUN mkdir -p ${DATAVAULT_HOME}/lib

COPY --from=0 /usr/local/bin/ep /usr/local/bin/ep
COPY --from=0 /usr/local/bin/wait-for-it /usr/local/bin/wait-for-it
COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/lib ${DATAVAULT_HOME}/lib
COPY --from=0 /usr/src/datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home/bin ${DATAVAULT_HOME}/bin

COPY docker/config ${DATAVAULT_HOME}/config
COPY docker/scripts ${DATAVAULT_HOME}/scripts

RUN adduser -D datavault
RUN chown -R datavault:datavault ${DATAVAULT_HOME}

WORKDIR ${DATAVAULT_HOME}/lib

ENTRYPOINT ["/docker_datavault-home/scripts/docker-entrypoint.sh", "worker"]
CMD ["java", "-cp", "datavault-worker-1.0-SNAPSHOT-jar:./*", "org.datavaultplatform.worker.WorkerManager"]
