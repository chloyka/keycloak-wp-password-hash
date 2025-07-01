package org.chloyka.wp;

import org.keycloak.Config;
import org.keycloak.credential.hash.PasswordHashProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class WpPasswordHashProviderFactory implements PasswordHashProviderFactory {
    public static final String PROVIDER_ID = "wp-phpass";

    public WpPasswordHashProvider create(KeycloakSession session) {
        return new WpPasswordHashProvider(PROVIDER_ID);
    }

    @Override
    public void init(Config.Scope scope) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public void close() {}
}