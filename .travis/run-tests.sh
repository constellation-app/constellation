#!/bin/bash
set -euo pipefail

echo "Run Core Build"
# core-build
ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=true update-dependencies-clean-build

echo "Run Core Unit Testing"
# core-unit-test
ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dtest.run.args=-javaagent:"${JACOCO_AGENT}" test

echo "Run Jacoco Processing"
# Need to convert the binary jacoco.exec files to XML since the property
# sonar.coverage.jacoco.xmlReportPaths on SonarCloud only supports XML
while IFS= read -r -d '' file; do
  classfile="$(echo "${file}" | cut -d "/" -f2)"
  xml_output="${file%.exec}.xml"
  java -jar "${JACOCO_HOME}/lib/jacococli.jar" report "${file}" --classfiles "${classfile}" --xml "${xml_output}"
done < <(find . -iname "*jacoco.exec" -print0)

echo "Run Core Training Build"
# core-training-build
ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=true build # clean build
# disable clean build to preserve files for Sonar
