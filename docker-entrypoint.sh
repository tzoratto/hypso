#!/usr/bin/env sh

set -e

delgroup ping
addgroup -g $DOCKER_GID docker
adduser -D -G docker hypso

exec su-exec hypso java -jar /home/hypso/hypso.jar