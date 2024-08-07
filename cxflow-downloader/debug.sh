#!/bin/bash
set -e

EXEC_PATH=$(dirname $(realpath $0) )

./gradlew build

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=32768 -jar $EXEC_PATH/build/libs/cxflow-download.jar $@
