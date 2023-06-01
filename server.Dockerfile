FROM ubuntu:20.04
RUN apt-get update && apt-get upgrade -y
RUN apt-get install openjdk-11-jdk -y
ADD ./build/libs/hierarchy.gmbh-0.0.1-SNAPSHOT.jar /server.jar
ENTRYPOINT java -jar /server.jar
