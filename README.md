# TQ Exchange Hub Monorepo

This repository now separates the frontend and backend implementations into dedicated folders so both stacks can live side by side.

## Project structure

```
├── backend   # Spring Boot REST API
└── frontend  # Vite + React client
```

## Frontend (React + Vite)

The original Lovable project now lives under [`frontend/`](frontend/). To start the development server:

```bash
cd frontend
npm install
npm run dev
```

All previous configuration files (Tailwind CSS, Supabase setup, etc.) remain unchanged inside this directory.

## Backend (Spring Boot)

A brand new Spring Boot project is available inside [`backend/`](backend/). It exposes a sample health endpoint at `GET /api/health` that returns a simple status payload so you can verify the service is running.

Common Maven commands:

```bash
cd backend
mvn spring-boot:run        # Start the API locally
mvn test                   # Run backend tests
```

The application listens on port `8080` by default (see `application.properties`). Feel free to expand this project with your domain logic and persistence needs.
