#!/usr/bin/env bash
pkill rmiregistry
rmiregistry &
kill $(ps aux | grep '[p]ython main.py' | awk '{print $2}')
java -classpath "commons-cli-1.3.1.jar:commons-codec-1.10.jar:gson-2.6.2.jar:slf4j-api-1.7.21.jar:slf4j-simple-1.7.21.jar:." crawler.main.Main ../config/peerList.txt
