FROM ubuntu:18.04
LABEL maintainer="Daniel Grant <daniel.grant@digirati.com>"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]
ARG DEBIAN_FRONTEND=noninteractive

ARG OPENJDK_8_JDK_HEADLESS_VERSION=8u212-b03-0ubuntu1.18.04.1
ARG MAVEN_VERSION=3.6.0-1~18.04.1
ARG PYTHON_VERSION=3.6.7-1~18.04
ARG PYTHON_SETUPTOOLS_VERSION=39.0.1-2
ARG PYTHON_PIP_VERSION=9.0.1-2.3~ubuntu1.18.04.1
ARG PYTHON_WHEEL_VERSION=0.30.0-0.2
ARG SOFTWARE_PROPERTIES_COMMON_VERSION=0.96.24.32.9
ARG APT_TRANSPORT_HTTPS_VERSION=1.6.11
ARG CURL_VERSION=7.58.0-2ubuntu3.7
ARG GNUPG_AGENT_VERSION=2.2.4-1ubuntu1.2
ARG SHELLCHECK_VERSION=0.4.6-1
ARG GIT_VERSION=1:2.17.1-1ubuntu0.4
ARG DOCKER_CE_VERSION=5:18.09.7~3-0~ubuntu-bionic
ARG PRE_COMMIT_VERSION=1.17.0

RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-8-jdk-headless maven=$MAVEN_VERSION python3=$PYTHON_VERSION \
       python3-setuptools=$PYTHON_SETUPTOOLS_VERSION python3-pip=$PYTHON_PIP_VERSION python3-wheel=$PYTHON_WHEEL_VERSION \
       software-properties-common=$SOFTWARE_PROPERTIES_COMMON_VERSION apt-transport-https=$APT_TRANSPORT_HTTPS_VERSION \
       curl=$CURL_VERSION gnupg-agent=$GNUPG_AGENT_VERSION shellcheck=$SHELLCHECK_VERSION git=$GIT_VERSION \
    && curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add - \
    && add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
    && apt-get update \
    && apt-get install -y --no-install-recommends docker-ce=$DOCKER_CE_VERSION \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && pip3 install pre-commit==$PRE_COMMIT_VERSION \
    && useradd -m -d /home/build -s /bin/bash -u 1010 build

ENV JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"

USER build
WORKDIR /home/build
