#!/bin/bash

source githubutilities/functions.sh

echo "Updating dependencies and building."

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=true update-dependencies-clean-build

echo "Finished Updating dependencies and building."

title "Run Sonar Scanning"

cd /tmp || exit
echo "Downloading sonar-scanner....."
if [ -d "/tmp/sonar-scanner-cli-3.2.0.1227-linux.zip" ];then
    sudo rm /tmp/sonar-scanner-cli-3.2.0.1227-linux.zip
fi
wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.0.0.1744-linux.zip
echo "Download completed."

echo "Unziping downloaded file..."
unzip sonar-scanner-cli-4.0.0.1744-linux.zip
echo "Unzip completed."
rm sonar-scanner-cli-4.0.0.1744-linux.zip

echo "Installing to opt..."
if [ -d "/var/opt/sonar-scanner-4.0.0.1744-linux" ];then
    rm -rf /var/opt/sonar-scanner-4.0.0.1744-linux
fi
mv sonar-scanner-4.0.0.1744-linux /var/opt/sonar-scanner
export PATH="$PATH:/var/opt/sonar-scanner/bin"
echo "Installation completed successfully."

if [ ! -z $2 ]; then
  if [ $1 != "constellation-app/constellation" ]; then
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
