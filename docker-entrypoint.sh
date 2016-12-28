#!/usr/bin/env sh

set -e

if [ $(getent group ping) ]; then
    delgroup ping
    addgroup -g $DOCKER_GID docker
    adduser -D -G docker hypso
fi

exec su-exec hypso java -jar /home/hypso/hypso.jar