#!/usr/bin/env bash
set -e

# Set default values
if [ -z ${NGINX_PORT} ]; then
	export NGINX_PORT=8070
fi
if [ -z ${MOTU_URL} ]; then
	export MOTU_URL=http://$(hostname):8080
fi
if [ -z ${MOTU_PATH} ]; then
	export MOTU_PATH=/motu-web
fi
if [ -z ${MOTU_DOWNLOAD_PATH} ]; then
	export MOTU_DOWNLOAD_PATH=/mis-gateway/deliveries
fi

envsubst '${NGINX_PORT} ${MOTU_URL} ${MOTU_PATH} ${MOTU_DOWNLOAD_PATH}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/frontal.conf

exec /usr/sbin/nginx -g 'daemon off;' || cat /etc/nginx/nginx.conf
