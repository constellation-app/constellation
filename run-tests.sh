#!/bin/bash
set -euo pipefail

source .travis/functions.sh

title "Run Core Build"

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=true update-dependencies-clean-build

title "Run Core Unit Testing"

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dtest.run.args=-javaagent:"${JACOCO_AGENT}" test

title "Run Jacoco Processing"

# Need to convert the binary jacoco.exec files to XML since the property
# sonar.coverage.jacoco.xmlReportPaths on SonarCloud only supports XML
while IFS= read -r -d '' file; do
  classfile="$(echo "${file}" | cut -d "/" -f2)"
  xml_output="${file%.exec}.xml"
  java -jar "${JACOCO_HOME}/lib/jacococli.jar" report "${file}" --classfiles "${classfile}" --xml "${xml_output}"
done < <(find . -iname "*jacoco.exec" -print0)
