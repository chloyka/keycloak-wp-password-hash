# WordPress Password Hash Provider for Keycloak

This project contains a custom `PasswordHashProvider` implementation that enables Keycloak to verify WordPress (PHPass) password hashes. It is helpful when migrating existing WordPress users to Keycloak because you can keep the original password hashes instead of forcing everyone to reset their password.

## Why build a custom container?

Keycloak requires any custom provider jars to be present during the build step. The `test/Dockerfile` builds Keycloak, copies this provider into the image and runs `kc.sh build`. This produces a new image (`kc:latest`) that contains the provider. Running vanilla Keycloak will not load the jar automatically.

## Prerequisites

- Java 11 or higher (this repo uses 21, but you can change it) and Maven installed for building the provider JAR
- Keycloak 20 or higher
- Docker and Docker Compose for running the test environment (optional)

## Building the Provider

1. Compile the project using Maven:
   ```bash
   mvn clean package
   ```
   The command produces `target/wordpress-password-hasher-1.0.0.jar`.
2. Create a directory for provider JARs used by the test container:
   ```bash
   mkdir -p test/providers
   cp target/wordpress-password-hasher-1.0.0.jar test/providers/
   ```

Alternatively, run the Makefile from the `test` directory which performs the Maven build:
```bash
cd test
make build
```

## Building the Keycloak Container

The standard Keycloak image does not contain this provider, so a new image must be built. Inside the `test` directory run:

```bash
make build-docker
```

This command uses `Dockerfile` to copy the provider JAR into the Keycloak image and execute `kc.sh build`. The resulting image is tagged `kc:latest` and is ready for use.

## Running the Test Environment

From the `test` directory start the container with Docker Compose:

```bash
docker-compose up -d
```

The service exposes Keycloak on port `8080`. Default admin credentials are defined through environment variables in `docker-compose.yaml` (`admin`/`secret`).

## Importing Users from WordPress

Export usernames/user emails and password hashes from your WordPress database. The table `wp_users` contains the hashed password in the `user_pass` column:

```sql
SELECT user_email, user_pass FROM wp_users;
```

Use these hashes when creating users in Keycloak. Below is an example JSON file `user.json`:

```json
{
  "username": "wpuser@example.com",
  "enabled": true,
  "credentials": [
    {
      "credentialData": "{\"algorithm\":\"wp-phpass\",\"hashIterations\":8}",
      "secretData":     "{\"value\":\"$P$B7x......\",\"salt\":\"\"}",
      "type": "password",
      "temporary": false
    }
  ]
}
```

## Adding a User via Keycloak REST API

1. Obtain an admin token:
   ```bash
   TOKEN=$(curl -s \
     -d "client_id=admin-cli" \
     -d "username=admin" \
     -d "password=secret" \
     -d "grant_type=password" \
     "http://localhost:8080/realms/master/protocol/openid-connect/token" | jq -r .access_token)
   ```
2. Create the user (replace `myrealm` with your realm name):
   ```bash
   curl -X POST "http://localhost:8080/admin/realms/myrealm/users" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     --data @user.json
   ```

## Testing the Login

After adding the user, verify that the password hash works by performing a password grant request:

```bash
curl -d "client_id=admin-cli" \
  -d "username=wpuser@example.com" \
  -d "password=<plaintext-password>" \
  -d "grant_type=password" \
  "http://localhost:8080/realms/myrealm/protocol/openid-connect/token"
```

If the hash matches, Keycloak returns an access token, confirming that the provider can validate WordPress passwords.

## Cleaning Up

To stop the test container:
```bash
docker-compose down
```
