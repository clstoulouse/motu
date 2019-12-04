FROM ${docker.base}

###
# Installing motu products
###

ARG MOTU_PRODUCTS_TOMCAT_VERSION=${motu.products.tomcat.version}
ARG OPENJDK_VERSION=${project.version.openjdk}

# This is introduced as a workaround for : https://github.com/fabric8io/docker-maven-plugin/issues/894
ENV DOCKER_JAVA_HOME /docker-java-home

ENV PACKAGE_NAME java-${OPENJDK_VERSION}-openjdk
ENV JAVA_HOME ${DOCKER_JAVA_HOME}

ENV BASE_INSTALL_DIR=/opt

ENV INSTALL_DIR=${BASE_INSTALL_DIR}/motu
ENV MOTU_PRODUCTS_DIR=${INSTALL_DIR}/products

ENV CATALINA_BASE=${INSTALL_DIR}/tomcat-motu \
	CATALINA_HOME=${MOTU_PRODUCTS_DIR}/apache-tomcat-${MOTU_PRODUCTS_TOMCAT_VERSION} \
	CDO_HOME=${MOTU_PRODUCTS_DIR}/cdo-group

ENV PATH="${CDO_HOME}:${PATH}" \
	LD_LIBRARY_PATH="${CDO_HOME}/lib:${LD_LIBRARY_PATH}"

ENV JMX_ENABLED=false \
	JVM_PORT_JMX=9010 \
	DEBUG_ENABLED=false \
	JVM_ADDRESS_DEBUG=7779
	
ENV APACHE_TOMCAT_VERSION=${MOTU_PRODUCTS_TOMCAT_VERSION}

RUN yum update -y && \
    yum install -y ${PACKAGE_NAME} && \
    yum install -y libgomp && \
    yum install -y make && \
    yum install -y gcc && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    find /tmp /var/tmp /run /var/log -mindepth 1 -delete -print && \
    ln -svT $(readlink -f /usr/bin/java | sed "s:/bin/java::") "${DOCKER_JAVA_HOME}" && \
    groupadd --gid ${docker.group.id} ${docker.group.name} && \
    useradd --uid ${docker.user.id} --gid ${docker.group.id} ${docker.user.name}

COPY ${docker.supporting-files.dir}/bin/*.sh ${INSTALL_DIR}/

RUN chown -R ${docker.user.name}:${docker.group.name} ${BASE_INSTALL_DIR} && \
	chmod +x ${INSTALL_DIR}/install-cots.sh && \
	chmod +x ${INSTALL_DIR}/products/cots-versions.sh && \
	chmod +x ${INSTALL_DIR}/products/cdo-group/*.sh && \
	ls -l /opt && \
	ls -l /opt/motu && \
	ls -l /opt/motu/products && \
	mkdir -p ${CATALINA_BASE}/conf \
		${CATALINA_BASE}/bin \
		${CATALINA_BASE}/lib \
		${CATALINA_BASE}/logs \
		${CATALINA_BASE}/webapps \
		${CATALINA_BASE}/work \
		${CATALINA_BASE}/temp && \
	${INSTALL_DIR}/install-cots.sh && \
	ls -l /opt && \
	ls -l /opt/motu && \
	ls -l /opt/motu/products && \
    cp -r ${CATALINA_HOME}/conf/* ${CATALINA_BASE}/conf && \
    chown -R ${docker.user.name}:${docker.group.name} ${CATALINA_BASE} ${ENTRYPOINTS_DIR}

WORKDIR ${INSTALL_DIR}

USER ${docker.user.name}
