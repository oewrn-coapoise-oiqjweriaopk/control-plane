# Control Plane Service

Spring Boot backend for the API gateway control plane. It exposes admin APIs for:

- gateway overview stats
- routes and upstream mappings
- policy rules
- API key lifecycle
- nodes and platform users

The service uses PostgreSQL as the system of record and projects runtime config into Redis so the data plane can read it with low latency.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web, Validation, JPA, Redis, Actuator
- PostgreSQL
- Redis

## Run infrastructure

```bash
docker compose up -d
```

To run the full backend stack including the Spring Boot app:

```bash
docker compose up -d --build
```

## Run the service

```bash
mvn spring-boot:run
```

By default it expects:

- PostgreSQL on `localhost:5432`
- Redis on `localhost:6379`
- Control plane on `localhost:8081`

Useful environment overrides:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/gateway_control
export DB_USERNAME=gateway
export DB_PASSWORD=gateway
export REDIS_HOST=localhost
export REDIS_PORT=6379
export SERVER_PORT=8081
```

## IntelliJ IDEA

The project now includes a minimal `.idea/` setup with:

- a Spring Boot run configuration named `ControlPlaneService`
- a Maven test run configuration named `ControlPlaneServiceTests`
- shell-script run configurations named `ControlPlaneDockerInfraUp`, `ControlPlaneDockerFullStackUp`, and `ControlPlaneDockerDown`
- Java 21 language level and UTF-8 project encoding

Recommended startup flow in IntelliJ:

1. Open `control-plane-service` as a project.
2. Let IntelliJ import the Maven model from `pom.xml`.
3. Make sure the project SDK is set to a Java 21 installation if IntelliJ does not auto-resolve it.
4. Use `ControlPlaneDockerInfraUp` if you want only PostgreSQL and Redis.
5. Run the `ControlPlaneService` configuration.

For presentation mode:

1. Run `ControlPlaneDockerFullStackUp`.
2. Open `http://localhost:8081/actuator/health`.
3. Open `http://localhost:8081/api/v1/overview`.
4. Run `ControlPlaneDockerDown` when you are done.

The default database URL now forces PostgreSQL to use `UTC` for the session timezone. This avoids startup failures on machines whose JVM default timezone is reported as `Asia/Calcutta`, which PostgreSQL rejects.

The application also forces the JVM default timezone to `UTC` at startup for the same reason.

By default CORS allows both `http://localhost:5173` and `http://localhost:8080` so the frontend can talk to the control plane from either common local dev port.

To verify it is working:

1. Open `http://localhost:8081/actuator/health` and confirm you get an HTTP 200 health response.
2. Call `http://localhost:8081/api/v1/overview` and confirm you receive JSON with route, policy, key, user, and node counts.
3. Call `http://localhost:8081/api/v1/routes` and confirm the seeded route list is returned.
4. Start the frontend and confirm it loads data instead of showing the control-plane unavailable banner.

## OpenAPI Docs

Springdoc OpenAPI is enabled for the control plane.

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- OpenAPI YAML: `http://localhost:8081/v3/api-docs.yaml`

The generated documentation includes summaries and schema descriptions for the control-plane endpoints and request payloads.

To view the docs locally:

1. Start the control plane.
2. Open `http://localhost:8081/swagger-ui.html` in your browser.
3. If you only want the raw spec, open `http://localhost:8081/v3/api-docs`.

## GitHub Pages API Docs

The repository now includes a Pages deployment workflow at [`.github/workflows/docs-pages.yml`](/home/ton3s/gateway-service/control-plane-service/.github/workflows/docs-pages.yml).

It:

- starts PostgreSQL and Redis in GitHub Actions
- boots the control plane
- downloads the generated OpenAPI JSON from `/v3/api-docs`
- publishes a static Swagger UI site to GitHub Pages

Before it will publish publicly, set GitHub Pages in the repository settings to use `GitHub Actions` as the build source.

If the app fails at startup, check the usual causes first:

- PostgreSQL is not running on `localhost:5432`
- Redis is not running on `localhost:6379`
- the IntelliJ SDK is not Java 21
- another process is already using port `8081`

## GitHub Actions

The project includes two GitHub Actions workflows in [`.github/workflows`](/home/ton3s/gateway-service/control-plane-service/.github/workflows):

- [`ci.yml`](/home/ton3s/gateway-service/control-plane-service/.github/workflows/ci.yml)
- [`tests.yml`](/home/ton3s/gateway-service/control-plane-service/.github/workflows/tests.yml)
  Runs `mvn clean test` on pushes, pull requests, and manual dispatch, then uploads Surefire reports.
- [`package.yml`](/home/ton3s/gateway-service/control-plane-service/.github/workflows/package.yml)
  Builds the Spring Boot jar on version tags like `v1.0.0` and uploads it as a workflow artifact.

The default CI build workflow now packages the project without tests, and test execution happens in the dedicated test workflow.

## IntelliJ HTTP Files

Ready-to-run IntelliJ `.http` request files are available in [`http/`](/home/ton3s/gateway-service/control-plane-service/http):

- [`control-plane-smoke.http`](/home/ton3s/gateway-service/control-plane-service/http/control-plane-smoke.http)
  Health and basic read checks
- [`control-plane-demo-flow.http`](/home/ton3s/gateway-service/control-plane-service/http/control-plane-demo-flow.http)
  Create demo API key, route, and policy
- [`control-plane-mutations.http`](/home/ton3s/gateway-service/control-plane-service/http/control-plane-mutations.http)
  Update, revoke, and delete operations
- [`control-plane-negative.http`](/home/ton3s/gateway-service/control-plane-service/http/control-plane-negative.http)
  Validation and not-found checks

Open any of them in IntelliJ and use the gutter "Run" action beside each request.

If this service moves into its own GitHub repository, these workflows will work from that repository root as-is.

## API surface

- `GET /api/v1/overview`
- `GET/POST/PUT/DELETE /api/v1/routes`
- `GET/POST/PUT/DELETE /api/v1/policies`
- `GET /api/v1/api-keys`
- `POST /api/v1/api-keys`
- `POST /api/v1/api-keys/{id}/revoke`
- `GET /api/v1/users`
- `GET /api/v1/nodes`
- `POST /api/v1/nodes/heartbeat`
- `GET /api/v1/metrics`

On startup the app seeds demo data when the database is empty so the frontend has immediate data to render.
