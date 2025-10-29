# TQ Exchange Hub Monorepo

This repository now separates the frontend and backend implementations into dedicated folders so both stacks can live side by side.

## Project structure

```
├── backend   # Spring Boot REST API
└── frontend  # Vite + React client
```

## Frontend (React + Vite)

The original Lovable project now lives under [`frontend/`](frontend/). The client expects an API base URL via the `VITE_API_BASE_URL` environment variable and falls back to `http://localhost:8080` when it is not provided. Create a `.env` file (or copy [`frontend/.env.example`](frontend/.env.example)) and set the value that matches where the backend is exposed.

```bash
cd frontend
npm install
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

## Backend (Spring Boot)

The Spring Boot service lives in [`backend/`](backend/). It now includes seeded categories, profiles, items, trades, messages and reviews so the frontend has meaningful data as soon as the API starts.

Common Maven commands:

```bash
cd backend
mvn spring-boot:run        # Start the API locally with the in-memory H2 database
mvn test                   # Run backend tests
```

### CORS configuration

CORS settings are externalised under the `application.cors.*` properties in [`backend/src/main/resources/application.properties`](backend/src/main/resources/application.properties). Override them with environment variables (for example `APPLICATION_CORS_ALLOWED-ORIGINS`) to match your deployment domain.

### Running both services with Docker Compose

The root [`docker-compose.yml`](docker-compose.yml) spins up the PostgreSQL database, the Spring Boot backend and the Vite frontend, wiring them with compatible URLs:

```bash
docker compose up --build
```

Services are exposed at:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080

The compose stack sets `VITE_API_BASE_URL` so the browser talks to the backend through the same origin that is published to the host machine.
