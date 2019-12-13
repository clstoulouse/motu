FROM ${docker.base}

###
# Installing motu products
###

ARG MOTU_VERSION=${project.version}
ARG MOTU_PRODUCTS_TOMCAT_VERSION=${motu.products.tomcat.version}
ARG OPENJDK_VERSION=${project.version.openjdk}

# This is introduced as a workaround for : https://github.com/fabric8io/docker-maven-plugin/issues/894
ENV DOCKER_JAVA_HOME /docker-java-home

ENV PACKAGE_NAME java-${OPENJDK_VERSION}-openjdk
ENV JAVA_HOME ${DOCKER_JAVA_HOME}

ENV BASE_INSTALL_DIR=/opt
ENV INSTALL_DIR=${BASE_INSTALL_DIR}/motu
ENV PRODUCTS_DIR=${INSTALL_DIR}/products

ENV CDO_HOME=${PRODUCTS_DIR}/cdo-group

ENV PATH="${CDO_HOME}:${PATH}" \
	LD_LIBRARY_PATH="${CDO_HOME}/lib:${LD_LIBRARY_PATH}"

ENV JMX_ENABLED=false \
	JVM_PORT_JMX=9010 \
	DEBUG_ENABLED=false \
	JVM_ADDRESS_DEBUG=7779

RUN yum update -y && \
    yum install -y ${PACKAGE_NAME} && \
    yum install -y libgomp && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    find /tmp /var/tmp /run /var/log -mindepth 1 -delete -print && \
    ln -svT $(readlink -f /usr/bin/java | sed "s:/bin/java::") "${DOCKER_JAVA_HOME}" && \
    mkdir -p ${INSTALL_DIR}

RUN ls -l /opt

RUN ls -l /opt/motu

COPY ${docker.supporting-files.dir} ${INSTALL_DIR}

RUN ls -l /opt

RUN ls -l /opt/motu

COPY ${docker.supporting-files.dir}/products ${PRODUCTS_DIR}/products

RUN ls -l /opt

RUN ls -l /opt/motu
RUN ls -l /opt/motu/products

RUN groupadd --gid ${docker.group.id} ${docker.group.name} && \
    useradd --uid ${docker.user.id} --gid ${docker.group.id} ${docker.user.name} && \
    chown -R ${docker.user.name}:${docker.group.name} ${PRODUCTS_DIR} && \
    chmod +x . ${PRODUCTS_DIR}/cots-versions.sh && \
    chmod +x . ${CDO_HOME}/cdo.sh && \
    . ${PRODUCTS_DIR}/cots-versions.sh

WORKDIR ${INSTALL_DIR}

USER ${docker.user.name}
