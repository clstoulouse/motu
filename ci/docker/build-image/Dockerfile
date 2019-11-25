FROM maven:3-jdk-8

RUN wget https://get.docker.com/ -O script.sh && \
    chmod +x script.sh && \
    ./script.sh && \
    rm ./script.sh && \
    apt-get update -y && \
    apt-get install -y unzip && \
    apt-get clean