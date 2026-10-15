# Event Booking API

A REST API for managing venues, their rooms, and the events held in them. Bookings and authentication are next.


## Data model

```
Venue 1 ──── * Room 1 ──── * Event
```

## Run it
```bash
docker compose up -d db     # PostgreSQL on localhost:5434
mvn spring-boot:run         # API on http://localhost:9090
```

Flyway creates the schema on startup from `src/main/resources/db/migration`.

```bash
cd frontend
npm install
npm run dev                 # http://localhost:5173
```

## TODO
-  Bookings with seat limits and row locking
-  JWT authentication with admin and user roles
-  Dockerfile for the API, plus a GitHub Actions CI pipeline
-  Notification microservice (Python / FastAPI)
-  Deployment to AWS
