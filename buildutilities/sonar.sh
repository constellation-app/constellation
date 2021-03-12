#!/bin/bash

source buildutilities/functions.sh

title "Run Sonar Scanning"

if [ ! -z $2 ]; then
  if [ $1 != "aldebaran30701/constellation" ]; then
    echo "skipping running sonar-scanner"
  else
    echo "This is a Pull Request"
    SONAR_PULLREQUEST_BRANCH="$(echo $1 | awk '{split($0,a,"/"); print a[1]}')/$4"
    sonar-scanner \
      -Dsonar.login="${SONAR_TOKEN}" \
      -Dsonar.pullrequest.key=$2 \
      -Dsonar.pullrequest.branch="${SONAR_PULLREQUEST_BRANCH}" \
      -Dsonar.pullrequest.base=$3
  fi
else
echo "Not a Pull Request"
  sonar-scanner \
    -Dsonar.login="${SONAR_TOKEN}" \
    -Dsonar.branch.name=$3
fi
