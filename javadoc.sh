#!/bin/sh

$JAVA_HOME/bin/javadoc -public -noindex -author -sourcepath ./src/java -d ./javadoc org.jdbcpersistence
