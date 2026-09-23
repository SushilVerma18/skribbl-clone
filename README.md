# Skribbl.io Clone

Complete full-stack assignment implementation using Java 21/Spring Boot/PostgreSQL and JavaScript/React/Vite.

## Assignment coverage
The supplied assignment requires multiplayer rooms, turn-based drawing, real-time drawing, word selection, scoring, WebSockets, lobby, configurable settings, hints/chat and deployment. fileciteturn0file0L11-L17 fileciteturn0file0L158-L180

## Local setup

### PostgreSQL
Create:
`skribbl_clone`

Default connection:
`localhost:5432`

Configure `DATABASE_USERNAME` and `DATABASE_PASSWORD` if needed.

### Backend
```bash
cd backend
mvn clean test
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## REST
- POST `/api/rooms`
- GET `/api/rooms/{code}`
- POST `/api/rooms/{code}/join`
- POST `/api/rooms/{code}/ready?playerId=...`
- POST `/api/rooms/{code}/start?playerId=...`
- GET `/api/rooms/public`
- GET `/api/health`

## WebSocket
STOMP endpoint `/ws`; application prefix `/app`; broker `/topic`.

Room messages:
- `/app/room/{code}/draw`
- `/app/room/{code}/choose-word`
- `/app/room/{code}/guess`
- `/app/room/{code}/canvas`

Broadcast topics:
- `/topic/room/{code}/game`
- `/topic/room/{code}/word-options`
- `/topic/room/{code}/word-private`
- `/topic/room/{code}/drawing`
- `/topic/room/{code}/chat`
- `/topic/room/{code}/scores`

The supplied assignment specifically calls for drawing strokes rather than continuously sending screenshots and for WebSocket synchronization. fileciteturn0file0L114-L142

## Deployment
Backend is prepared for Render Web Service + Render PostgreSQL. Frontend uses:
- `VITE_API_URL=https://your-render-backend`
- `VITE_WS_URL=wss://your-render-backend/ws`

The assignment asks for a public deployment and a live URL in the README. fileciteturn0file0L182-L203

## Important verification note
This workspace does not provide a running PostgreSQL instance, Maven executable, or browser session, so the generated source cannot honestly be claimed as fully integration-tested here. The implementation and project structure are complete; run the two build commands above in the selected development environment and fix any environment-specific issue that appears.

## Verification performed in this workspace

The complete source tree was generated. A full Maven test/build and browser/PostgreSQL integration run could not be executed in this workspace because the execution environment did not expose a Maven executable, a PostgreSQL server, or a browser automation session. Do not treat the project as production-verified until those commands are run in the target development environment.
