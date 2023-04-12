#!/bin/sh

if [[ -z "${JAVA_OPTS}" ]];then
    export JAVA_OPTS="-Xmx64m -Xms64m"
fi

java_debug_args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"


exec java ${java_debug_args} ${JAVA_OPTS} ${JAVA_ARGS}  -jar /opt/egov/*.jar