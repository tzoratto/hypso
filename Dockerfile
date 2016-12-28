FROM openjdk:8u111-jre-alpine

MAINTAINER Thomas Zoratto <thomas.zoratto@gmail.com>

ENV DOCKER_VERSION=1.12.1 \
    DOCKER_COMPOSE_VERSION=1.8

RUN apk --update --no-cache \
    add curl py-pip su-exec && \
    rm -rf /var/cache/apk/* && \
    curl https://get.docker.com/builds/Linux/x86_64/docker-${DOCKER_VERSION}.tgz | tar zx && \
    mv /docker/* /bin/ && chmod +x /bin/docker* && \
    pip install docker-compose==${DOCKER_COMPOSE_VERSION} && \
    apk del curl

WORKDIR /home/hypso

COPY target/hypso-*.jar ./hypso.jar

ENV SCRIPT /home/hypso/script/deploy.sh
ENV DOCKER_GID 999

EXPOSE 4567

COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

CMD ["/docker-entrypoint.sh"]