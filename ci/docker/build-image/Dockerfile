FROM maven:3-jdk-8

RUN wget https://get.docker.com/ -O script.sh && \
    chmod +x script.sh && \
    ./script.sh && \
    rm ./script.sh && \
    apt-get update -y && \
    apt-get install -y unzip && \
    apt-get install -y rpm && \
    apt-get install -y cpio && \
    apt-get install -y make && \
    apt-get install -y build-essential && \
    apt-get install -y m4 && \
    apt-get clean
#    apt-get install -y gcc && \