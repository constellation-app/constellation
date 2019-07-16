#!/bin/bash

echo $NETBEANS_HOME
echo ${RELEASE}

# core-dist-zip
ant -Dnbplatform.active.dir=$NETBEANS_HOME -Dnbplatform.default.netbeans.dest.dir=$NETBEANS_HOME -Dnbplatform.default.harness.dir=$NETBEANS_HOME/harness -Dbuild.compiler.debug=true build-zip
