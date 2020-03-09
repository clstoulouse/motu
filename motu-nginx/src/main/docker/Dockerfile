FROM ${docker.base}

COPY ${docker.supporting-files.dir}/ ${docker.working.dir}

COPY nginx-frontal.conf.template /etc/nginx/conf.d/default.conf.template

WORKDIR ${docker.working.dir}

USER root

RUN touch /var/run/nginx.pid && \
    chown -R ${docker.user.name}:${docker.user.name} /var/run/nginx.pid && \
    chown -R ${docker.user.name}:${docker.user.name} /var/cache/nginx && \
    mkdir /var/www && \
    chown -R ${docker.user.name}:${docker.user.name} /var/www && \
    chown -R ${docker.user.name}:${docker.user.name} ${docker.working.dir} && \
    chmod 0775 ${docker.working.dir}/bin/* && \
    rm /etc/nginx/conf.d/default.conf && \
    chown ${docker.user.name}:${docker.user.name} /etc/nginx/conf.d && \
    mkdir -p /var/log/nginx && \
    chown -R ${docker.user.name}:${docker.user.name} /var/log/nginx

USER ${docker.user.name}

ENTRYPOINT ["${docker.working.dir}/bin/docker-entrypoint.sh"]
