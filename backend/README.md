# Backend application

This directory contains the Spring Boot backend for the TQ Exchange Hub project.

## Getting started

```bash
cd backend
mvn spring-boot:run
```

The sample project exposes a `GET /api/health` endpoint that returns `{ "status": "UP" }` so you can confirm the service is running.

## Default accounts for local testing

Flyway seeds demo data in the in-memory H2 database, including an admin account you can use to access the `/api/admin/**` routes:

- Admin: `maria@example.com` / `password123`
