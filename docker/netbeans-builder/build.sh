#!/bin/bash

VERSION=latest # use latest for local builds
BASEDIR=$(dirname "$0")
IMAGENAME="constellation-app/netbeans-builder"

docker build -t ${IMAGENAME}:"${VERSION}" -f "${BASEDIR}"/Dockerfile "${BASEDIR}"
