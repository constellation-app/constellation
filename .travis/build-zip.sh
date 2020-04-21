#!/bin/bash
set -euo pipefail

source .travis/functions.sh

title "Build Windows Zip"

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dbuild.compiler.debug=true build-zip-with-windows-jre

title "Build Linux Zip"

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dbuild.compiler.debug=true build-zip-with-linux-jre

title "Build MacOSX Zip"

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dbuild.compiler.debug=true build-zip-with-macosx-jre

#title "Build Portable Zip"
#
#ant \
#  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
#  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
#  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
#  -Dbuild.compiler.debug=true download-dependencies build-zip