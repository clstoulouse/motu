#!/bin/bash

run_parts() {
	echo "docker-entrypoint: running scripts from '${1}'..."
	for f in "${1}"/*; do
		case "$f" in
			*.sh)     echo "docker-entrypoint: running $f"; . "$f" ;;
			*)        echo "docker-entrypoint: ignoring $f" ;;
		esac
		echo
	done
}

if [ "$1" == "/catalina-run" ]; then
	if [[ -d "$ENTRYPOINTS_DIR" ]]; then
		run_parts "$ENTRYPOINTS_DIR" && \
	  		echo "All entry-point scripts executed successfully" || \
	  		exit 1
	fi
fi

if [ -z "$(ls -A $MOTU_CONFIG_DIR)" ]; then
   cp -r $MOTU_DEFAULT_DIR/* $MOTU_CONFIG_DIR
fi

exec "$@"
