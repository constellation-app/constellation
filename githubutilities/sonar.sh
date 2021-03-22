#!/bin/bash

source githubutilities/functions.sh

title "Updating dependencies and building"

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=true update-dependencies-clean-build

title "Run Sonar Scanning"

if [ ! -z $2 ]; then
  if [ $1 != "aldebaran30701/constellation" ]; then
    echo "skipping running sonar-scanner"
  else
    SONAR_PULLREQUEST_BRANCH="$(echo $1 | awk '{split($0,a,"/"); print a[1]}')/$4"
    sonar-scanner \
      -Dsonar.login=$5 \
      -Dproject.settings=/home/runner/work/constellation/constellation/sonar-project.properties \
      -Dsonar.pullrequest.key=$2 \
      -Dsonar.pullrequest.branch="${SONAR_PULLREQUEST_BRANCH}" \
      -Dsonar.pullrequest.base=$3
  fi
else
  sonar-scanner \
    -Dsonar.login=$5 \
    -Dproject.settings=/home/runner/work/constellation/constellation/sonar-project.properties \
    -Dsonar.branch.name=$3
fi
