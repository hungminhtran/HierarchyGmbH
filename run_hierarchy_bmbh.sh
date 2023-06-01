#!/bin/sh
./gradlew $1 build
docker-compose up --no-deps --build server
