FROM bitnami/keycloak:26.2.5 AS builder

COPY providers/*.jar /opt/bitnami/keycloak/providers/

RUN /opt/bitnami/keycloak/bin/kc.sh build

FROM bitnami/keycloak:26.2.5

COPY --from=builder /opt/bitnami/keycloak/ /opt/bitnami/keycloak/

EXPOSE 8080
