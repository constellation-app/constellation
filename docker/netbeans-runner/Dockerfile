FROM ubuntu:bionic

LABEL \
  IMAGE="netbeans-runner:12" \
  SOURCE="ubuntu:bionic"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get -y update \
    && apt-get install -y --no-install-recommends \
    ca-certificates \
    unzip \
    wget \
    python3 \
    git \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV ZULU_VERSION="zulu11.39.15-ca-fx-jdk11.0.7-linux_x64"
ENV ZULU_CHECKSUM="3d68aeccf6bba70618eb047b2d0bf221418b5f1befc09d9e86fe17899e19f681d32a952130fe8b4c0b43d934e81cf587c857b0539c3fc21ffe3f888ab0546683  ${ZULU_VERSION}.tar.gz"

RUN wget -q "https://cdn.azul.com/zulu/bin/${ZULU_VERSION}.tar.gz" \
    && echo "${ZULU_CHECKSUM}" | sha512sum -c \
    && tar -xf "${ZULU_VERSION}.tar.gz" \
    && mkdir -p /usr/java/ \
    && mv "${ZULU_VERSION}" /usr/java/ \
    && rm "${ZULU_VERSION}.tar.gz"

ENV NETBEANS_VERSION="12.0" \
    NETBEANS_HOME="/usr/local/netbeans" \
    NETBEANS_HELP_GITHUB="https://github.com/constellation-app/third-party-dependencies/blob/master/NetBeans%20Help"
ENV NETBEANS_CHECKSUM="91030c9628a08acd85f1a58a7f71eec2e57dc85841c1c475cc6311335b5d8cdc10c1198274b9668b7f61a28d04b07661247dc1c3e36b8e29214aec3748e499e4  netbeans-${NETBEANS_VERSION}-bin.zip"

COPY common.xml.modified ./

RUN wget -q "http://apache.mirror.amaze.com.au/netbeans/netbeans/${NETBEANS_VERSION}/netbeans-${NETBEANS_VERSION}-bin.zip" \
    && echo "${NETBEANS_CHECKSUM}" | sha512sum -c \
    && unzip -q "netbeans-${NETBEANS_VERSION}-bin.zip" \
    && wget -q "${NETBEANS_HELP_GITHUB}/org-netbeans-modules-javahelp.jar?raw=true" -O netbeans/platform/modules/org-netbeans-modules-javahelp.jar \
    && wget -q "${NETBEANS_HELP_GITHUB}/jhall-2.0_05.jar?raw=true" -O netbeans/platform/modules/ext/jhall-2.0_05.jar \
    && mv common.xml.modified "netbeans/harness/common.xml" \
    && mv netbeans "${NETBEANS_HOME}" \
    && rm "netbeans-${NETBEANS_VERSION}-bin.zip"

ENV JACOCO_VERSION="0.8.5" \
    JACOCO_HOME="/opt/jacoco"
ENV JACOCO_CHECKSUM="2be070c33da2b9152ec9cd9f42f39190e6cbe9f245627cbd1a6750a07fb380be113b1e63da47738377d76d3f739f75d94b60b618a8c17a5c7581312ffdf2dfc3  jacoco-${JACOCO_VERSION}.zip"

RUN mkdir -p ${JACOCO_HOME}/ \
    && wget -q "http://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/${JACOCO_VERSION}/jacoco-${JACOCO_VERSION}.zip" -O jacoco-${JACOCO_VERSION}.zip \
    && echo "${JACOCO_CHECKSUM}" | sha512sum -c \
    && mv "jacoco-${JACOCO_VERSION}.zip" "${JACOCO_HOME}/" \
    && unzip -q "${JACOCO_HOME}/jacoco-${JACOCO_VERSION}.zip" -d "${JACOCO_HOME}/" \
    && rm "${JACOCO_HOME}/jacoco-${JACOCO_VERSION}.zip"

ENV ANT_HOME="${NETBEANS_HOME}/extide/ant" \
    JAVA_HOME="/usr/java/${ZULU_VERSION}" \
    JACOCO_AGENT="${JACOCO_HOME}/lib/jacocoagent.jar"
ENV PATH="${PATH}:${ANT_HOME}/bin:${JAVA_HOME}/bin"
