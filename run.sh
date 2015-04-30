#!/bin/sh
CLASSPATH=${SEN_HOME}/lib/sen.jar
CLASSPATH=${CLASSPATH}:${SEN_HOME}/lib/commons-logging.jar:.

java -Dsen.home=${SEN_HOME} -classpath ${CLASSPATH}:. $1 $2

