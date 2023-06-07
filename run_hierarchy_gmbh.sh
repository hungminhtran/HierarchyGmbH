#!/bin/sh
set -e
find ./src -name "*" |  xargs java -jar google-java-format-1.17.0-all-deps.jar -a -r

./gradlew $1 build
docker buildx build -f server.Dockerfile --allow network.host  -t hierarchygmbh_server:latest .
docker-compose up -d
