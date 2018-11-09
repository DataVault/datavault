FROM maven:3.5.2-jdk-8

ENV MAVEN_OPTS "-Xmx1024m"

RUN curl -sLo /usr/local/bin/ep https://github.com/kreuzwerker/envplate/releases/download/v0.0.8/ep-linux && chmod +x /usr/local/bin/ep
RUN curl -sLo /usr/local/bin/wait-for-it https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh && chmod +x /usr/local/bin/wait-for-it

# Copy just the pom files to cache the dependencies
#COPY datavault-assembly/pom.xml /tmp/datavault-assembly/pom.xml
#COPY datavault-broker/pom.xml /tmp/datavault-broker/pom.xml
#COPY datavault-common/pom.xml /tmp/datavault-common/pom.xml
#COPY datavault-webapp/pom.xml /tmp/datavault-webapp/pom.xml
#COPY datavault-worker/pom.xml /tmp/datavault-worker/pom.xml
#COPY pom.xml /tmp
#COPY datavault-common/src/main/resources/ftm-api-2.4.2.jar /tmp
#COPY datavault-common/src/main/resources/low-level-api-core-1.14.19.jar /tmp
#WORKDIR /tmp
# RUN mvn install:install-file -Dfile=/tmp/ftm-api-2.4.2.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=ftm-api -Dversion=2.4.2 -Dpackaging=jar
# RUN mvn install:install-file -Dfile=/tmp/low-level-api-core-1.14.19.jar -DgroupId=oracle.cloudstorage.ftm -DartifactId=low-level-api-core -Dversion=1.14.19 -Dpackaging=jar

#RUN mvn dependency:go-offline --fail-never
# The dependency:go-offline gets a lot of the dependencies, but not all. This would get more, but not sure about other implications
#RUN mvn -s /usr/share/maven/ref/settings-docker.xml install --fail-never

WORKDIR /usr/src

# By default this is empty, but if you've built the package locally (without Docker) you can speed up repeated builds by copying your ~/.m2/repository into docker/m2/repository
# Any dependencies you don't have will still be downloaded as normal
COPY docker/m2/repository /root/.m2/repository

COPY . /usr/src

RUN mvn clean
RUN mvn -Dmaven.test.skip=true package