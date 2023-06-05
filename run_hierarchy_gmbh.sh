#!/bin/sh
set -e
./gradlew format
./gradlew $1 build
docker buildx build -f server.Dockerfile --allow network.host  -t hierarchygmbh_server:latest .
docker-compose up -d
