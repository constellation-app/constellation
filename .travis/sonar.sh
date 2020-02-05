#!/usr/bin/env sh

if [ "${TRAVIS_PULL_REQUEST}" != "false" ]; then
  SONAR_PULLREQUEST_BRANCH="$(echo $TRAVIS_PULL_REQUEST_SLUG | awk '{split($0,a,"/"); print a[1]}')/$TRAVIS_PULL_REQUEST_BRANCH"
  sonar-scanner sonarqube \
    -Dsonar.login="${SONAR_TOKEN}" \
    -Dsonar.pullrequest.key="${TRAVIS_PULL_REQUEST}" \
    -Dsonar.pullrequest.branch="${SONAR_PULLREQUEST_BRANCH}" \
    -Dsonar.pullrequest.base="${TRAVIS_BRANCH}"
else
  sonar-scanner sonarqube \
    -Dsonar.login="${SONAR_TOKEN}" \
    -Dsonar.branch.name="${TRAVIS_BRANCH}"
fi
