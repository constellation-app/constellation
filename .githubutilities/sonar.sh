#!/bin/bash

source .githubutilities/functions.sh

title "Run Sonar Scanning"

if [ ! -z $2 ]; then
  if [ $1 != "constellation-app/constellation" ]; then
    echo "skipping running sonar-scanner"
  else
    SONAR_PULLREQUEST_BRANCH="$(echo $1 | awk '{split($0,a,"/"); print a[1]}')/$4"
    sonar-scanner \
      -Dsonar.login=$5 \
      -Dproject.settings=./sonar-project.properties \
      -Dsonar.pullrequest.key=$2 \
      -Dsonar.pullrequest.branch="${SONAR_PULLREQUEST_BRANCH}" \
      -Dsonar.pullrequest.base=$3
  fi
else
  sonar-scanner \
    -Dsonar.login=$5 \
    -Dproject.settings=./sonar-project.properties \
    -Dsonar.branch.name=$3
fi
