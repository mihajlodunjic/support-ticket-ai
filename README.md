# Intelligent Helpdesk Backend

Spring Boot backend for an intelligent helpdesk/ticketing system with:

- ticket creation and admin management
- AI category prediction integration with fallback behavior
- automatic priority assignment
- JWT-based authentication for admin endpoints
- PostgreSQL + Flyway persistence
- Swagger/OpenAPI documentation

The backend root project is in `backend/`.

## Current backend capabilities

- `POST /api/tickets` creates a ticket
- `POST /api/predict` calls the external AI prediction service
- `POST /api/auth/register` registers a user
- `POST /api/auth/login` returns a JWT token
- `GET /api/auth/me` returns the currently authenticated user
- `GET /api/admin/tickets` lists and filters tickets
- `GET /api/admin/tickets/{id}` returns ticket details
- `PUT /api/admin/tickets/{id}` updates admin-managed ticket fields
- `GET /api/admin/statistics` returns dashboard statistics
- `GET /api/health` returns application health

The backend works without a frontend. You can use Swagger UI or `curl`/Postman directly.

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- Flyway
- PostgreSQL
- H2 for tests
- Maven Wrapper

## Requirements

- Java 21
- Docker Desktop or Docker Engine for PostgreSQL/Dockerized startup
- optional external AI HTTP service exposing `POST /predict`

## Local startup

The easiest local flow is:

1. Prepare environment variables or `backend/.env`.
2. Start PostgreSQL with Docker Compose from `backend/`.
3. Start Spring Boot with the `local` profile.

### 1. Prepare environment variables

If you want to use the bundled Docker PostgreSQL service, first create `backend/.env` based on `backend/.env.example`.

You can also export values directly in PowerShell:

```powershell
$env:POSTGRES_PASSWORD="change-me-postgres-password"
$env:APP_SECURITY_JWT_SECRET="replace-with-a-secret-of-at-least-32-characters"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/helpdesk"
$env:SPRING_DATASOURCE_USERNAME="helpdesk"
$env:SPRING_DATASOURCE_PASSWORD="change-me-postgres-password"
```

### 2. Start PostgreSQL

From `backend/`:

```powershell
docker compose up postgres -d
```

### 3. Run the backend

If you already placed values in `backend/.env`, export only the runtime variables Spring needs in your current shell, or define them in your IDE run configuration.

Optional admin seed user:

```powershell
$env:APP_SECURITY_ADMIN_NAME="Admin User"
$env:APP_SECURITY_ADMIN_EMAIL="admin@example.com"
$env:APP_SECURITY_ADMIN_PASSWORD="ChangeMe123!"
```

Optional AI service override:

```powershell
$env:APP_AI_BASE_URL="http://localhost:8000"
```

From `backend/`:

```powershell
.\mvnw.cmd spring-boot:run
```

The default profile is `local`, so no extra profile flag is required.

## Docker startup

The repository includes:

- `backend/Dockerfile`
- `backend/docker-compose.yaml`
- `backend/.env.example`

From `backend/`:

1. Create `backend/.env` based on `backend/.env.example`
2. Start the stack:

```powershell
docker compose up --build
```

Notes:

- `APP_SECURITY_JWT_SECRET` is required.
- PostgreSQL password is read from env, not hardcoded in the compose file.
- `APP_AI_BASE_URL` defaults to `http://host.docker.internal:8000` inside Docker so the backend can reach an AI service running on the host.
- If no AI service is running, `POST /api/tickets` still works through the fallback flow.
- Direct `POST /api/predict` requires the AI HTTP service to be available.

## Profiles

- `local`
  Uses PostgreSQL and Flyway, intended for local development.
- `test`
  Uses in-memory H2 and Flyway, intended for automated tests.
- `prod`
  Uses externally provided PostgreSQL configuration and env-driven secrets.

## Environment variables

Required in `local`/`prod`:

- `APP_SECURITY_JWT_SECRET`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Optional:

- `APP_SECURITY_TOKEN_VALIDITY_SECONDS`
- `APP_SECURITY_ADMIN_NAME`
- `APP_SECURITY_ADMIN_EMAIL`
- `APP_SECURITY_ADMIN_PASSWORD`
- `APP_AI_BASE_URL`
- `APP_AI_PREDICT_PATH`
- `APP_AI_TIMEOUT_SECONDS`
- `APP_AI_FALLBACK_CATEGORY`
- `APP_AI_TOP_PREDICTIONS_LIMIT`
- `APP_CORS_ALLOWED_ORIGINS`

Test profile uses deterministic test-only values from `application-test.yaml`.

## Swagger / OpenAPI

After the backend starts:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Admin endpoints and `/api/auth/me` support JWT authorization through Swagger.

## API examples

### Create a public ticket

```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{
    "title": "VPN is not working",
    "description": "Cannot connect to the company VPN since this morning.",
    "userEmail": "user@example.com"
  }'
```

### Register a user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@example.com",
    "password": "ChangeMe123!"
  }'
```

### Login and get a JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "ChangeMe123!"
  }'
```

### Read the current authenticated user

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <jwt-token>"
```

### List admin tickets

```bash
curl "http://localhost:8080/api/admin/tickets?page=0&size=20&status=NEW" \
  -H "Authorization: Bearer <jwt-token>"
```

### Read statistics

```bash
curl http://localhost:8080/api/admin/statistics \
  -H "Authorization: Bearer <jwt-token>"
```

### Direct AI prediction request

```bash
curl -X POST http://localhost:8080/api/predict \
  -H "Content-Type: application/json" \
  -d '{
    "text": "The printer is jammed and users cannot print."
  }'
```

## Error response contract

All handled API errors return the same shape:

```json
{
  "timestamp": "2026-06-03T18:54:55.670712Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/tickets",
  "validationErrors": [
    {
      "field": "title",
      "message": "must not be blank"
    }
  ]
}
```

## Verification

From `backend/`:

```powershell
.\mvnw.cmd test
```

Optional Docker verification:

```powershell
docker compose up --build
```

## Notes and limitations

- The Spring Boot backend does not require a frontend to run.
- The backend expects an external AI HTTP service; the fallback flow allows ticket creation even when the AI service is unavailable.
- The repository contains AI-related Python assets under `predict_python_service/`, but the Spring app does not load the `.keras` model directly.
