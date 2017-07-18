#!/usr/bin/env bash

mvn clean install

nohup java -jar target/*jar-with-dependencies*jar &

