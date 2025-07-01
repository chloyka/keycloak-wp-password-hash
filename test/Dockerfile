ARG BASE_IMAGE=bitnami/keycloak:26.2.5
ARG VERSION=1.0.0

FROM busybox as downloader
ARG VERSION
RUN wget -O /wordpress-password-hasher-${VERSION}.jar \
    "https://github.com/chloyka/keycloak-wp-password-hash/releases/download/v${VERSION}/wordpress-password-hasher-${VERSION}.jar"

FROM $BASE_IMAGE AS builder
ARG VERSION
COPY --from=downloader /wordpress-password-hasher-${VERSION}.jar /opt/bitnami/keycloak/providers/wordpress-password-hasher-${VERSION}.jar
RUN /opt/bitnami/keycloak/bin/kc.sh build

FROM $BASE_IMAGE
COPY --from=builder /opt/bitnami/keycloak/ /opt/bitnami/keycloak/