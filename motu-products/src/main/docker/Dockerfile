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
ENV PATH="${CDO_HOME}:${PATH}"

ENV JMX_ENABLED=false \
	JVM_PORT_JMX=9010 \
	DEBUG_ENABLED=false \
	JVM_ADDRESS_DEBUG=7779

COPY ${docker.supporting-files.dir} ${INSTALL_DIR}/

RUN yum update -y && \
    yum install -y ${PACKAGE_NAME} && \
    yum install -y libgomp && \
    yum install -y tomcat-native && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    find /tmp /var/tmp /run /var/log -mindepth 1 -delete -print && \
    ln -svT $(readlink -f /usr/bin/java | sed "s:/bin/java::") "${DOCKER_JAVA_HOME}" && \
    groupadd --gid ${docker.group.id} ${docker.group.name} && \
    useradd --uid ${docker.user.id} --gid ${docker.group.id} ${docker.user.name} && \
    ls -l ${INSTALL_DIR} && \
    ls -l ${INSTALL_DIR}/bin && \
    ls -l ${INSTALL_DIR}/docker-entrypoint.d && \
    mv ${INSTALL_DIR}/bin/cdo.sh ${CDO_HOME}/. && \
    chmod u+x ${CDO_HOME}/cdo.sh && \
    chown -R ${docker.user.name}:${docker.group.name} ${PRODUCTS_DIR} && \
    mkdir /lib/hdf5 /lib/zlib /lib/netcdf && \
    mv ${PRODUCTS_DIR}/cdo-group/hdf5-*-install/lib/* /lib/hdf5 && \
    rm -rf ${PRODUCTS_DIR}/cdo-group/zlib-*-install/lib/pkgconfig && \
    mv ${PRODUCTS_DIR}/cdo-group/zlib-*-install/lib/* /lib/zlib && \
    rm -rf ${PRODUCTS_DIR}/cdo-group/netcdf-c-*-install/lib/pkgconfig && \
    mv ${PRODUCTS_DIR}/cdo-group/netcdf-c-*-install/lib/* /lib/netcdf && \
    mv ${PRODUCTS_DIR}/cdo-group/cdo-*-install/bin/* /usr/local/bin && \
    chown -h -R root:root /lib/hdf5 /lib/zlib /lib/netcdf && \
    chown -h -R root:root /usr/local/bin/* && \
    chmod 755 /lib/hdf5/* /lib/zlib/* /lib/netcdf/* && \
    chmod 755 /usr/local/bin/* && \
    rm -r ${CDO_HOME}/hdf* ${CDO_HOME}/zlib* ${CDO_HOME}/netcdf* ${CDO_HOME}/cdo-* && \
    mv ${INSTALL_DIR}/bin/ld.so.conf /etc/ld.so.conf && \
    rm -r ${INSTALL_DIR}/bin && \
    ldconfig

WORKDIR ${INSTALL_DIR}

USER ${docker.user.name}
