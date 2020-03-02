#!/bin/bash

BASEDIR=$(dirname "$0")
image_name="constellationapplication/netbeans-runner"

build() {
  local dockerfile="${1}"
  local tag="${2}"
  docker build -t ${image_name}:"${tag}" -f "${BASEDIR}/${dockerfile}" "${BASEDIR}"
}

images=(
  "Dockerfile.netbeans-8.2=8.2"
  "Dockerfile.netbeans-11=11"
)

for image in "${images[@]}"; do
  dockerfile=$(awk -F "=" '{print $1}' <<< "${image}")
  tag=$(awk -F "=" '{print $2}' <<< "${image}")
  build "${dockerfile}" "${tag}"
done
