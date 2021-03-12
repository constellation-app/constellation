#!/bin/bash

source buildutilities/functions.sh

title "Run Sonar Scanning"

if [ ! -z "$2" ]; then
  if [ $1 != "aldebaran30701/constellation" ]; then
    echo $1
    echo "skipping running sonar-scanner"
  else
    echo "in else"
    echo $1
    echo $2
    echo $3
    echo $4
    SONAR_PULLREQUEST_BRANCH="$(echo $1 | awk '{split($0,a,"/"); print a[1]}')/$4"
    sonar-scanner \
      -Dsonar.login="${SONAR_TOKEN}" \
      -Dsonar.pullrequest.key=$2 \
      -Dsonar.pullrequest.branch="${SONAR_PULLREQUEST_BRANCH}" \
      -Dsonar.pullrequest.base=$3
  fi
else
  sonar-scanner \
    -Dsonar.login="${SONAR_TOKEN}" \
    -Dsonar.branch.name=$3
fi
