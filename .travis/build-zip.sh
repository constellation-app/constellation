#!/bin/bash
set -euo pipefail

title "Build Portable Zip"

ant \
  -Dnbplatform.active.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.netbeans.dest.dir="${NETBEANS_HOME}" \
  -Dnbplatform.default.harness.dir="${NETBEANS_HOME}"/harness \
  -Dbuild.compiler.debug=true download-dependencies build-zip
