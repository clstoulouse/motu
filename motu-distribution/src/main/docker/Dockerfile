FROM ${docker.base}

###
# Installing motu products
###

ARG MOTU_PRODUCTS_TOMCAT_VERSION=${motu.products.tomcat.version}

ENV BASE_INSTALL_DIR=/opt \
	ENTRYPOINTS_DIR=/docker-entrypoint.d

ENV INSTALL_DIR=${BASE_INSTALL_DIR}/motu
ENV MOTU_PRODUCTS_DIR=${INSTALL_DIR}/products

ENV CATALINA_BASE=${INSTALL_DIR}/tomcat-motu \
	CATALINA_HOME=${MOTU_PRODUCTS_DIR}/apache-tomcat-${MOTU_PRODUCTS_TOMCAT_VERSION}
	
WORKDIR ${docker.working.dir}
	
RUN pwd
RUN ls -l
RUN	ls -l /opt
RUN	ls -l /opt/motu
RUN	ls -l /opt/motu-distribution

COPY ${docker.supporting-files.dir}/bin/catalina-run.sh /catalina-run

COPY ${docker.supporting-files.dir}/bin/docker-entrypoint.sh /docker-entrypoint.sh

COPY ${docker.supporting-files.dir} ${INSTALL_DIR}/

RUN tar -xf ${INSTALL_DIR}/*-products*.tar.gz -C ${BASE_INSTALL_DIR} && \
    tar -xf ${INSTALL_DIR}/*-distribution*.tar.gz -C ${BASE_INSTALL_DIR} && \
    chown -R ${docker.user.name}:${docker.group.name} ${BASE_INSTALL_DIR} && \
    chown ${docker.user.name}:${docker.group.name} /catalina-run /docker-entrypoint.sh && \
    mkdir ${ENTRYPOINTS_DIR} && \
	chmod +x /docker-entrypoint.sh && \
	chmod +x /catalina-run && \
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
	ls -l /opt && \
	ls -l /opt/motu && \
	ls -l /opt/motu/products && \
    cp -r ${CATALINA_HOME}/conf/* ${CATALINA_BASE}/conf && \
    chown -R ${docker.user.name}:${docker.group.name} ${CATALINA_BASE} ${ENTRYPOINTS_DIR}

WORKDIR ${INSTALL_DIR}

EXPOSE 8080 8443 8009

USER ${docker.user.name}

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["/catalina-run"]
