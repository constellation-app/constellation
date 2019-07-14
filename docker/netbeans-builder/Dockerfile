FROM ubuntu:18.04

RUN apt-get -y update \
      && apt-get install -y \
      wget \
      unzip \
      openjfx

ENV JAVA_VERSION=11.0.3 \
  JDK_VERSION=zulu11.31.11-ca-fx-jdk11.0.3-linux_x64

ENV JAVA_CHECKSUM="1c34d1ee82326f0e4b981828dd33239d  jdk${JAVA_VERSION}-linux_x64.tar.gz"

# Needs to be downloaded into the same directory as this file
# RUN wget -q https://cdn.azul.com/zulu/bin/zulu8.38.0.13-ca-fx-jdk${JAVA_VERSION}-linux_x64.tar.gz

COPY jdk${JAVA_VERSION}-linux_x64.tar.gz .
RUN echo "${JAVA_CHECKSUM}" | sha512sum -c \
      && tar -xf jdk${JAVA_VERSION}-linux_x64.tar.gz \
      && mkdir -p /usr/java/ \
      && mv jdk${JDK_VERSION} /usr/java/ \
      && rm jdk${JAVA_VERSION}-linux_x64.tar.gz

ENV ANT_VERSION=1.9.9 \
  ANT_HOME=/opt/ant \
  JAVA_HOME=/usr/java/jdk$JDK_VERSION

ENV ANT_CHECKSUM="4d9e0f372bd9401993a4aed3b3c754813192b8d3093e772c6eb70465a7ae3c6de9ccc13d399d6df9b784a87a6504e4c6526d27a47887e4d89911438133745e0e  apache-ant-${ANT_VERSION}-bin.tar.gz"

RUN wget -q https://archive.apache.org/dist/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz \
      && echo "${ANT_CHECKSUM}" | sha512sum -c \
      && tar -xzf apache-ant-${ANT_VERSION}-bin.tar.gz \
      && mv apache-ant-${ANT_VERSION} $ANT_HOME \
      && rm -v apache-ant-${ANT_VERSION}-bin.tar.gz

ENV NETBEANS_VERSION=11.0 \
  NETBEANS_BUNDLE=incubating-netbeans-11.0-bin.zip

ENV NETBEANS_CHECKSUM="9d7fbe5c6bcf781fc1d3f8e2aee62db0435dd716c60dc73ef900ee2817473cc5b0a8e12c1453b7e57aedcece70cff778673a8cf563c1fa4eea816d9636955d4b  incubating-netbeans-11.0-bin.zip"

RUN wget -q https://www-us.apache.org/dist/incubator/netbeans/incubating-netbeans/incubating-11.0/incubating-netbeans-11.0-bin.zip \
      && echo "${NETBEANS_CHECKSUM}" | sha512sum -c \
      && ./incubating-netbeans-11.0-bin.zip --silent \
      && rm $NETBEANS_BUNDLE

ENV JACOCO_VERSION=0.8.1 \
  JACOCO_HOME=/opt/jacoco

ENV JACOCO_CHECKSUM="f6d67930c701c87f47ec1ee53e3b4ff932909063c3efcbcf3ff207990d5c6a9fbf3eec90276d01bc02b5eebce732eb9525f651684189c0813f7a75ffa9d744aa  jacoco-${JACOCO_VERSION}.zip"

RUN mkdir -p $JACOCO_HOME/ \
      && wget "http://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/${JACOCO_VERSION}/jacoco-${JACOCO_VERSION}.zip" -O jacoco-${JACOCO_VERSION}.zip \
      && echo "${JACOCO_CHECKSUM}" | sha512sum -c \
      && mv jacoco-${JACOCO_VERSION}.zip $JACOCO_HOME/ \
      && unzip $JACOCO_HOME/jacoco-${JACOCO_VERSION}.zip -d $JACOCO_HOME/ \
      && rm $JACOCO_HOME/jacoco-${JACOCO_VERSION}.zip

ENV NETBEANS_HOME=/usr/local/netbeans-${NETBEANS_VERSION} \
  JACOCO_AGENT=${JACOCO_HOME}/lib/jacocoagent.jar \
  PATH=${PATH}:$ANT_HOME/bin:$JAVA_HOME/bin