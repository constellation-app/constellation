#!/bin/bash

echo $NETBEANS_HOME
echo ${RELEASE}

# core-build
ant -Dnbplatform.active.dir=$NETBEANS_HOME -Dnbplatform.default.netbeans.dest.dir=$NETBEANS_HOME -Dnbplatform.default.harness.dir=$NETBEANS_HOME/harness -Dupdate.dependencies=true -Dbuild.compiler.debug=true clean download-dependencies build

# core-unit-test
ant -Dnbplatform.active.dir=$NETBEANS_HOME -Dnbplatform.default.harness.dir=$NETBEANS_HOME/harness -Dnbplatform.default.netbeans.dest.dir=$NETBEANS_HOME -Dtest.run.args=-javaagent:$JACOCO_AGENT test

# core-training-build
ant -Dnbplatform.default.netbeans.dest.dir=$NETBEANS_HOME -Dnbplatform.default.harness.dir=$NETBEANS_HOME/harness -Dupdate.dependencies=true -Dbuild.compiler.debug=truenbplatform.default.netbeans.dest.dir=$NETBEANS_HOME -Dnbplatform.default.harness.dir=$NETBEANS_HOME/harness -Dupdate.dependencies=true -Dbuild.compiler.debug=true clean download-dependencies build
