#!/bin/bash
pkill rmiregistry
rmiregistry &
java -classpath "commons-cli-1.3.1.jar:commons-codec-1.10.jar:gson-2.6.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.21.jar:." -Dorg.slf4j.simpleLogger.defaultLogLevel=debug crawler.dht.ChordNode -i 0
