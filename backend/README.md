# Backend Deploy Notes

For the full backend overview, local startup and API notes, see the repository root `README.md`.

## Render Docker deploy

This backend is ready to run on Render as a Docker web service.

### Important

- `EXPOSE 10000` in `Dockerfile` is not enough by itself.
- The Spring Boot app must also listen on the runtime port.
- The application is configured to bind to `0.0.0.0` and to use the `PORT` environment variable.

### Render service setup

Use the backend folder as the Docker build context.

You can do that in either of these ways:

- Set Render `Root Directory` to `backend` and use `Dockerfile`.
- Or configure the service so the Docker build context is `/backend` and the Dockerfile used is `/backend/Dockerfile`.

This matters because the Dockerfile copies `.mvn`, `mvnw`, `pom.xml` and `src/` from the backend directory context.

### Recommended environment variables

Required:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_SECURITY_JWT_SECRET`

Render/runtime port:

- Leave Render default `PORT` value in place, or set `PORT=10000` explicitly.

Optional:

- `APP_SECURITY_TOKEN_VALIDITY_SECONDS`
- `APP_SECURITY_ADMIN_NAME`
- `APP_SECURITY_ADMIN_EMAIL`
- `APP_SECURITY_ADMIN_PASSWORD`
- `APP_AI_BASE_URL`
- `APP_CORS_ALLOWED_ORIGINS`

### Docker notes

- `Dockerfile` already builds the jar from the backend directory and copies `target/helpdesk-backend.jar`.
- `Dockerfile` already exposes port `10000`.
- `docker-compose.yaml` passes `PORT=10000` so the local Docker service matches the container port mapping.
