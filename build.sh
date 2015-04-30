#!/bin/sh
CLASSPATH=${SEN_HOME}/lib/sen.jar
CLASSPATH=${CLASSPATH}:${SEN_HOME}/lib/commons-logging.jar:.

javac -classpath ${CLASSPATH} $1

