#!/bin/bash
shopt -s globstar
javac -classpath "libs/commons-cli-1.3.1.jar:libs/commons-codec-1.10.jar:libs/gson-2.6.2.jar:libs/slf4j-api-1.7.21.jar:libs/slf4j-simple-1.7.21.jar" -d bin **/*.java
cp libs/*.jar bin/
