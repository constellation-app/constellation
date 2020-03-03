#!/bin/bash
set -euo pipefail

# core-build
ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=true update-dependencies-clean-build

# core-unit-test
ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dtest.run.args=-javaagent:"${JACOCO_AGENT}" test

# Need to convert the binary jacoco.exec files to XML since the property
# sonar.coverage.jacoco.xmlReportPaths on SonarCloud only supports XML
while IFS= read -r -d '' file; do
  classfile="$(echo "${file}" | cut -d "/" -f2)"
  xml_output="${file%.exec}.xml"
  java -jar "${JACOCO_HOME}/lib/jacococli.jar" report "${file}" --classfiles "${classfile}" --xml "${xml_output}"
done < <(find . -iname "*jacoco.exec" -print0)

# core-training-build
ant \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=truenbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dupdate.dependencies=true \
  -Dbuild.compiler.debug=true build # clean build
# disable clean build to preserve files for Sonar
