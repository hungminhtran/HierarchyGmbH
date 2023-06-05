FROM ubuntu:22.04
RUN apt-get update && apt-get upgrade -y
RUN apt-get install mysql-server -y
RUN apt install iputils-ping -y
RUN apt install dnsutils -y
RUN apt install mysql-client -y
RUN apt install net-tools -y

ENTRYPOINT ["tail -f /dev/stdout"]
