# Backend application

This directory contains the Spring Boot backend for the TQ Exchange Hub project.

## Getting started

```bash
cd backend
mvn spring-boot:run
```

The sample project exposes a `GET /api/health` endpoint that returns `{ "status": "UP" }` so you can confirm the service is running.

## Poblar la base de datos con 100k registros de prueba

Al iniciar la aplicación, Flyway ejecuta automáticamente la migración `V5__seed_100k_synthetic_data.sql`, la cual genera 100,000 perfiles, cuentas de usuario e ítems sintéticos basados en los registros de ejemplo incluidos en `V1__create_schema_and_seed.sql`.

La migración es compatible tanto con PostgreSQL como con H2 (usado en pruebas locales), por lo que no requiere extensiones específicas de base de datos.

Si no deseas cargar estos datos masivos (por ejemplo, en ambientes de producción), desactiva Flyway en las propiedades de arranque o elimina la migración antes de desplegar.
