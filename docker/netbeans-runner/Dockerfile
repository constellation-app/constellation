FROM ubuntu:bionic

LABEL \
  IMAGE="netbeans-runner:21" \
  SOURCE="ubuntu:bionic"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

RUN apt-get -y update \
    && apt-get install -y --no-install-recommends \
    ca-certificates \
    unzip \
    wget \
    python3 \
    git \
    libx11-dev \
    fontconfig \
    ttf-dejavu \
    libpangoft2-1.0-0 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

ENV SONAR_ZIP_FILENAME="sonar-scanner-cli-4.0.0.1744-linux.zip" \
    SONAR_FILENAME="sonar-scanner-4.0.0.1744-linux" \
    SONAR_DOWNLOAD_DIR="/tmp" \
    SONAR_HOME="/var/opt/sonar-scanner" 
ENV SONAR_DOWNLOAD_URL="https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/${SONAR_ZIP_FILENAME}"

RUN cd "${SONAR_DOWNLOAD_DIR}" \
    && echo "Downloading sonar-scanner....." \
    && if [ -d "/tmp/${SONAR_ZIP_FILENAME}" ] ; then sudo rm "${SONAR_DOWNLOAD_DIR}"/"${SONAR_ZIP_FILENAME}" ; fi \
    && wget -q "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.0.0.1744-linux.zip" \
    && echo "Download completed. Unziping downloaded file..." \
    && unzip "${SONAR_ZIP_FILENAME}" \
    && echo "Unzip completed." \
    && rm "${SONAR_ZIP_FILENAME}" \
    && echo "Installing to ${SONAR_HOME}" \
    && if [ -d "/var/opt/${SONAR_FILENAME}" ] ; then rm -rf /var/opt/"${SONAR_FILENAME}" ; fi \
    && mv "${SONAR_FILENAME}" "${SONAR_HOME}" \
    && echo "Installation completed successfully." 

ENV ZULU_VERSION="zulu21.32.17-ca-fx-jdk21.0.2-linux_x64"
ENV ZULU_CHECKSUM="08433694c8f264b592d6d05c4b5b414d4c41c00cb35b7beba515855554855466  ${ZULU_VERSION}.tar.gz"

RUN wget -q "https://cdn.azul.com/zulu/bin/${ZULU_VERSION}.tar.gz" \
    && echo "${ZULU_CHECKSUM}" | sha256sum -c \
    && tar -xf "${ZULU_VERSION}.tar.gz" \
    && mkdir -p /usr/java/ \
    && mv "${ZULU_VERSION}" /usr/java/ \
    && rm "${ZULU_VERSION}.tar.gz"

ENV NETBEANS_VERSION="21" \
    NETBEANS_HOME="/usr/local/netbeans" \
    NETBEANS_HELP_GITHUB="https://github.com/constellation-app/third-party-dependencies/blob/master/NetBeans%20Help"
ENV NETBEANS_CHECKSUM="1bd5695755eb9547d88a5539a6e43884d787dba4da6ecadc1b261e07c1dda56d0a03986a66755c5d3a358216711c0393ac8fdd649e15c953bdfe380cb564ca65  netbeans-${NETBEANS_VERSION}-bin.zip"

COPY common.xml.modified ./

RUN wget -q "https://dlcdn.apache.org/netbeans/netbeans/{NETBEANS_VERSION}/netbeans-${NETBEANS_VERSION}-bin.zip" \
    && echo "${NETBEANS_CHECKSUM}" | sha512sum -c \
    && unzip -q "netbeans-${NETBEANS_VERSION}-bin.zip" \
    && wget -q "${NETBEANS_HELP_GITHUB}/org-netbeans-modules-javahelp.jar?raw=true" -O netbeans/platform/modules/org-netbeans-modules-javahelp.jar \
    && wget -q "${NETBEANS_HELP_GITHUB}/jhall-2.0_05.jar?raw=true" -O netbeans/platform/modules/ext/jhall-2.0_05.jar \
    && mv common.xml.modified "netbeans/harness/common.xml" \
    && mv netbeans "${NETBEANS_HOME}" \
    && rm "netbeans-${NETBEANS_VERSION}-bin.zip"

ENV JACOCO_VERSION="0.8.11" \
    JACOCO_HOME="/opt/jacoco"
ENV JACOCO_CHECKSUM="facd1b38b44225871de5312b0d3ecd9ded009790512e32ee17fc3605c12171400b1dc8467c399757e0c00399a2913206a4a1d977555734e84af9fa2c0225e90a  jacoco-${JACOCO_VERSION}.zip"

RUN mkdir -p ${JACOCO_HOME}/ \
    && wget -q "http://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/${JACOCO_VERSION}/jacoco-${JACOCO_VERSION}.zip" -O jacoco-${JACOCO_VERSION}.zip \
    && echo "${JACOCO_CHECKSUM}" | sha512sum -c \
    && mv "jacoco-${JACOCO_VERSION}.zip" "${JACOCO_HOME}/" \
    && unzip -q "${JACOCO_HOME}/jacoco-${JACOCO_VERSION}.zip" -d "${JACOCO_HOME}/" \
    && rm "${JACOCO_HOME}/jacoco-${JACOCO_VERSION}.zip"

ENV ANT_HOME="${NETBEANS_HOME}/extide/ant" \
    JAVA_HOME="/usr/java/${ZULU_VERSION}" \
    JACOCO_AGENT="${JACOCO_HOME}/lib/jacocoagent.jar"
ENV PATH="${PATH}:${ANT_HOME}/bin:${JAVA_HOME}/bin:${SONAR_HOME}/bin"
