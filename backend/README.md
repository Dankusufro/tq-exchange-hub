# Backend application

This directory contains the Spring Boot backend for the TQ Exchange Hub project.

## Getting started

```bash
cd backend
mvn spring-boot:run
```

The sample project exposes a `GET /api/health` endpoint that returns `{ "status": "UP" }` so you can confirm the service is running.

### Database migrations

Flyway is enabled for every profile so migrations run automatically when the application starts. If you need to trigger them manually (for
example, after repairing an existing database), run:

```bash
cd backend
mvn flyway:migrate
```

If a previous run failed and the database already exists without the `is_suspended` column, execute `mvn flyway:repair` followed by
`mvn flyway:migrate`. For the in-memory H2 dev profile, restarting the app is enough to recreate the schema and apply all migrations,
including `V6__add_is_suspended_to_user_accounts.sql`.

## Default accounts for local testing

Flyway seeds demo data in the in-memory H2 database, including an admin account you can use to access the `/api/admin/**` routes:

- Admin: `maria@example.com` / `password123`
