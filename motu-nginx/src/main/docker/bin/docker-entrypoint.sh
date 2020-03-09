#!/usr/bin/env bash
set -e

# Set default values
if [ -z ${NGINX_PORT} ]; then
	export NGINX_PORT=8070
fi
if [ -z ${MOTU_PORT} ]; then
	export MOTU_PORT=8080
fi
if [ -z ${MOTU_HOST} ]; then
	export MOTU_HOST=$(hostname)
fi
if [ -z ${MOTU_PATH} ]; then
	export MOTU_PATH=/motu-web/Motu
fi
if [ -z ${MOTU_DOWNLOAD_PATH} ]; then
	export MOTU_DOWNLOAD_PATH=/mis-gateway/deliveries
fi

envsubst '${NGINX_PORT} ${MOTU_PORT} ${MOTU_HOST} ${MOTU_PATH} ${MOTU_DOWNLOAD_PATH}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/frontal.conf

exec /usr/sbin/nginx -g 'daemon off;' || cat /etc/nginx/nginx.conf
