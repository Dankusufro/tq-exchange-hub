# Backend application

This directory contains the Spring Boot backend for the TQ Exchange Hub project.

## Getting started

```bash
cd backend
mvn spring-boot:run
```

The sample project exposes a `GET /api/health` endpoint that returns `{ "status": "UP" }` so you can confirm the service is running.

## Poblar la base de datos con 100k registros de prueba

Para generar el esquema completo y cargar 100,000 filas sintéticas para pruebas de rendimiento, ejecuta el script SQL incluido:

```bash
psql -U <usuario> -d <base_de_datos> -f backend/db/seed-100k.sql
```

El script recrea las tablas, inserta los datos de ejemplo iniciales y luego genera perfiles, usuarios e ítems adicionales con valores aleatorios para que puedas probar el backend con mayor volumen de datos.
