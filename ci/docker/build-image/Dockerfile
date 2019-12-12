FROM registry-ext.cls.fr:443/docker-cls/os/centos7:20191202

# This is introduced as a workaround for : https://github.com/fabric8io/docker-maven-plugin/issues/894
ENV DOCKER_JAVA_HOME /docker-java-home

ENV OPENJDK_VERSION=1.8.0

ENV PACKAGE_NAME java-${OPENJDK_VERSION}-openjdk
ENV JAVA_HOME ${DOCKER_JAVA_HOME}

RUN wget https://get.docker.com/ -O script.sh && \
    chmod +x script.sh && \
    ./script.sh && \
    rm ./script.sh && \
    yum update -y && \
    yum install -y unzip && \
    yum install -y rpm && \
    yum install -y cpio && \
    yum install -y make && \
    yum install -y build-essential && \
    yum install -y m4 && \
    yum install -y ${PACKAGE_NAME} && \
    yum install -y dnf && \
    dnf install maven && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    find /tmp /var/tmp /run /var/log -mindepth 1 -delete -print && \
    ln -svT $(readlink -f /usr/bin/java | sed "s:/bin/java::") "${DOCKER_JAVA_HOME}" && \
    dnf clean
#    apt-get install -y gcc && \