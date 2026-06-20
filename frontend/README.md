# support-ticket-ai frontend

React SPA frontend for the `support-ticket-ai` project. It talks only to the Spring Boot backend in `/backend` and covers public ticket creation, authentication, admin statistics, admin ticket management and direct AI prediction testing.

## Stack

- React
- TypeScript
- Vite
- React Router
- Plain CSS
- `fetch` via a small internal API client

## Install and run

From `/frontend`:

```bash
npm install
npm run dev
```

For a production build:

```bash
npm run build
```

Optional local preview after build:

```bash
npm run preview
```

## Environment configuration

Create a `.env` file in `/frontend` based on `.env.example`:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

The frontend reads the backend base URL from `import.meta.env.VITE_API_BASE_URL`.

If the variable is missing, the code falls back to:

```text
http://localhost:8080
```

That fallback is intentional so the SPA still works in the common local setup, but using `.env` is preferred.

## Backend URL

- Default local backend URL: `http://localhost:8080`
- Expected dev frontend origin: `http://localhost:5173`
- Backend CORS already allows `http://localhost:5173` in the current backend configuration

## Main routes

- `/` - overview and backend health check
- `/tickets/new` - public ticket creation form
- `/login` - JWT login
- `/register` - user registration
- `/predict-test` - direct `POST /api/predict` test page
- `/admin` - admin statistics dashboard
- `/admin/tickets` - admin ticket table with filters and pagination
- `/admin/tickets/:id` - admin ticket details and update form

## Auth notes

- Login stores the JWT token in `localStorage`
- Admin requests send `Authorization: Bearer <token>`
- `/api/auth/me` is used to restore the authenticated user when possible
- Admin endpoints require a valid JWT and an admin-capable account on the backend
