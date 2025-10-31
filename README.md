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

### API documentation

Once the backend is running you can explore every endpoint (and try requests) through the automatically generated Swagger UI:

- Swagger UI: http://localhost:8080/swagger-ui/index.html

The OpenAPI specification served at `/v3/api-docs` is also checked in CI to ensure it stays available.

### Trade receipt workflow

Accepted trades now produce a signed PDF receipt that can be downloaded or emailed:

- `GET /api/trades/{id}/receipt` generates (or reuses) the PDF and returns it with the `X-Receipt-Hash`
  and `X-Receipt-Signature` headers so clients can verify integrity.
- `POST /api/trades/{id}/receipt/email` sends the same document as an attachment to the provided email
  when the mail service is configured.

The frontend surfaces these actions once a trade switches to the **accepted** state, allowing users to
download the receipt immediately or trigger an email delivery while visual feedback reports progress and
potential errors.

### Running both services with Docker Compose

The root [`docker-compose.yml`](docker-compose.yml) spins up the PostgreSQL database, the Spring Boot backend and the Vite frontend, wiring them with compatible URLs:

```bash
docker compose up --build
```

Services are exposed at:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080

The compose stack sets `VITE_API_BASE_URL` so the browser talks to the backend through the same origin that is published to the host machine.

### Observability and alerting

The backend now ships with Spring Boot Actuator and a Prometheus registry. Metrics are exposed at
`/actuator/prometheus` and the `/api/health` endpoint delegates to the Actuator health checks so the probes reflect the current
application state.

- Prometheus scrape and alert configuration lives under [`monitoring/prometheus`](monitoring/prometheus).
- Grafana provisioning (data source and dashboards) lives under [`monitoring/grafana`](monitoring/grafana).

To spin up Prometheus and Grafana locally you can extend the root `docker-compose.yml` with the monitoring services:

```yaml
services:
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./monitoring/prometheus/alert.rules.yml:/etc/prometheus/alert.rules.yml:ro
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
    ports:
      - '9090:9090'
  grafana:
    image: grafana/grafana:latest
    depends_on:
      - prometheus
    volumes:
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
    ports:
      - '3000:3000'
```

#### RNF alert thresholds

- **RNF1 (Performance):** `histogram_quantile(0.95, http_server_requests_seconds_bucket)` must stay below **0.5 seconds**. The
  `RNF1LatencyDegradation` alert fires after five minutes above the limit.
- **RNF3 (Availability):** Successful request ratio must stay above **99.5%**. The `RNF3AvailabilityDrop` alert fires after
  ten minutes with an error rate above 0.5%.

When an alert fires, acknowledge it in your alerting tool and:

1. Inspect the **Exchange Hub Service Overview** Grafana dashboard (provisioned automatically from
   [`monitoring/grafana/provisioning/dashboards/exchange-hub-dashboard.json`](monitoring/grafana/provisioning/dashboards/exchange-hub-dashboard.json)).
2. Correlate spikes in latency or error rate with rate limiter activity and application logs (now structured JSON) to identify
   offending clients or endpoints.
3. If RNF3 is breached, evaluate upstream dependencies (database, third-party APIs) and consider temporarily reducing allowed
   throughput via the rate limiter to preserve the error budget.

### Automated load testing

The GitHub Actions workflow [`performance-tests.yml`](.github/workflows/performance-tests.yml) runs a k6 scenario against the
`/api/health` probe and publishes a Markdown summary and JSON results as build artifacts. Configure the
`PERF_TEST_BASE_URL` repository variable with the publicly reachable backend URL to enable the job. To reproduce the test
locally run:

```bash
cd backend
k6 run performance/health-check.js --summary-export k6-summary.json
```
