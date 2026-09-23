Skribbl.io Clone

A full-stack multiplayer drawing and guessing game inspired by skribbl.io.

Players can create or join rooms, wait in a lobby, take turns drawing a selected word, and guess the word in real time.

Live Demo

Frontend: https://skribbl-clone-2-gnt5.onrender.com

Backend: https://skribbl-clone-1-4wgm.onrender.com

GitHub: https://github.com/SushilVerma18/skribbl-clone

Features

Rooms & Lobby

Create public or private rooms

Configure room settings

Join using a room code

Player list and host information

Ready status

Host-controlled game start

Share room/invite information

Gameplay

Multiplayer real-time gameplay

Turn-based drawing

Private word selection

Real-time drawing synchronization

Guessing and scoring

Leaderboard

Round progression

Countdown timer

Hints

In-game chat

Final game state

Drawing

Brush/pen

Colors

Brush size

Eraser

Undo

Clear canvas

Only the current drawer can draw during the drawing phase

Assignment Coverage

The project covers the core requirements of the supplied Skribbl.io Clone assignment:

Multiplayer rooms

Public/private rooms

Room creation and joining

Room code/invite flow

Lobby and player list

Ready status

Host-controlled start

Turn-based drawing

Real-time drawing

Word selection

Guessing

Scoring and leaderboard

Round progression and game completion

Drawing tools

WebSocket communication

Configurable room settings

Hints, chat, and countdown/timer

Public deployment

The assignment also requires setup information, a live URL, and an architecture/code overview covering WebSockets, canvas, and game logic.

Tech Stack

Layer

Technology

Frontend

JavaScript, React, Vite, CSS

Drawing

HTML5 Canvas

Real-time

STOMP over WebSockets

Backend

Java 21, Spring Boot

Backend APIs

Spring Web, Spring WebSocket

Persistence

Spring Data JPA, Hibernate

Database

PostgreSQL

Deployment

Render, Docker

Version Control

Git, GitHub

Architecture

React + Vite Frontend
        │
        ├── REST API
        │
        └── WebSocket / STOMP
                │
                ▼
        Spring Boot Backend
          │             │
          │             └── Game / WebSocket Logic
          │
          └── PostgreSQL

The backend manages room operations, player sessions, game state, turns, words, drawing updates, guesses, scores, timers, and game progression.

Game Flow

Create / Join Room
        ↓
      Lobby
        ↓
   Ready Players
        ↓
    Start Game
        ↓
   Choose Word
        ↓
     Drawing
        ↓
    Guessing
        ↓
     Scoring
        ↓
   Round End
        ↓
   Next Turn
        ↓
    Game Over

Room and Lobby

A player creates a room or joins an existing room using a room code. Players wait in the lobby, mark themselves ready, and the host starts the game.

Room settings include options such as maximum players, rounds, drawing time, word count, hints, word mode, and public/private visibility.

Word Selection

The current drawer receives multiple word options privately and selects one. The selected word is stored by the backend and used for guess validation.

Drawing and Guessing

The drawer draws the selected word on the canvas. Drawing actions are sent through WebSockets and synchronized with the other players.

Players who are not drawing submit guesses. Correct guesses update the player's score and are reflected in the room.

Round and Game End

A round can end when the timer reaches zero or the required players correctly guess the word. The backend rotates the drawer for the next turn and eventually displays the final leaderboard.

WebSocket Communication

The application uses STOMP over WebSockets for real-time gameplay.

Endpoint:

/ws

Application prefix:

/app

Client-to-Server

/app/room/{code}/draw
/app/room/{code}/choose-word
/app/room/{code}/guess
/app/room/{code}/canvas
/app/room/{code}/state

Server-to-Client

/topic/room/{code}/game
/topic/room/{code}/drawing
/topic/room/{code}/chat
/topic/room/{code}/scores
/topic/room/{code}/timer
/topic/room/{code}/hint

Private Destinations

/user/queue/word-options
/user/queue/word-private

Private destinations keep word options and the selected word available only to the drawer.

Real-Time Drawing

Canvas input
    ↓
Frontend captures drawing action
    ↓
WebSocket message
    ↓
Spring Boot WebSocket controller
    ↓
Game service
    ↓
Broadcast drawing update
    ↓
Other players update their canvas

The application synchronizes drawing actions rather than continuously sending screenshots.

Game State

The backend controls the main game state, including players, current drawer, round, game phase, selected word, word options, scores, timer, and turn order.

LOBBY
  ↓
WORD_SELECTION
  ↓
DRAWING
  ↓
ROUND_END
  ↓
WORD_SELECTION
  ↓
DRAWING
  ↓
GAME_OVER

The backend determines which actions are allowed during each phase.

REST API

Method

Endpoint

Purpose

POST

/api/rooms

Create room

GET

/api/rooms/{code}

Get room

POST

/api/rooms/{code}/join

Join room

POST

/api/rooms/{code}/ready

Update ready status

POST

/api/rooms/{code}/start

Start game

GET

/api/rooms/public

Get public rooms

GET

/api/health

Health check

REST APIs handle room/player operations. Real-time gameplay uses WebSockets.

Player Authentication

Players receive a session token when they create or join a room.

The frontend uses the token for authenticated REST requests and the WebSocket connection.

The backend validates the session before allowing authenticated actions such as drawing, word selection, guessing, canvas commands, and game-state requests.

Project Structure

skribbl-clone/
├── backend/
│   ├── src/main/java/com/example/skribbl/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── websocket/
│   │   └── security/
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   ├── api.js
│   │   └── websocket.js
│   ├── package.json
│   └── vite.config.js
└── README.md

Local Setup

Prerequisites

Java 21

Maven

Node.js

npm

PostgreSQL

Git

PostgreSQL

Create a database named:

skribbl_clone

Default local connection:

localhost:5432

Use environment variables for credentials:

DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

Do not commit real passwords or secrets to GitHub.

Backend

cd backend
mvn clean test
mvn spring-boot:run

Backend runs on:

http://localhost:8080

Frontend

cd frontend
npm install
npm run dev

Frontend normally runs on:

http://localhost:5173

Environment Variables

Backend

DATABASE_URL=jdbc:postgresql://localhost:5432/skribbl_clone
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
FRONTEND_URL=http://localhost:5173

Frontend

Local:

VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws

Production:

VITE_API_URL=https://skribbl-clone-1-4wgm.onrender.com
VITE_WS_URL=wss://skribbl-clone-1-4wgm.onrender.com/ws

Deployment

The application is deployed using Render.

React + Vite
     ↓
Render Static Site
     ↓
Spring Boot Web Service
     ↓
Render PostgreSQL

The backend is packaged using Docker and deployed with Java 21. The frontend is built using Vite and the generated dist directory is deployed as the static site.

Production URLs

Frontend: https://skribbl-clone-2-gnt5.onrender.com

Backend: https://skribbl-clone-1-4wgm.onrender.com

GitHub: https://github.com/SushilVerma18/skribbl-clone

How to Play

Open the application.

Create a room or join an existing room.

Wait for other players to join.

Players mark themselves ready.

The host starts the game.

The drawer receives word options.

The drawer selects a word.

The drawer draws the word.

Other players guess the word.

Correct guesses receive points.

The round ends.

The drawer changes.

The game continues for the configured rounds.

The final leaderboard is displayed.

Code Walkthrough

Drawing Flow

Canvas
  ↓
Drawing Action
  ↓
WebSocket
  ↓
WebSocket Controller
  ↓
Game Service
  ↓
Broadcast
  ↓
Other Players

Game Flow

Room
  ↓
Game State
  ↓
Current Drawer
  ↓
Word Selection
  ↓
Drawing
  ↓
Guessing
  ↓
Scoring
  ↓
Next Turn

WebSocket Flow

React Client
     ↕
STOMP / WebSocket
     ↕
Spring Boot
     ↕
Game Service

Security and Configuration

Player session tokens are used for authenticated game actions.

The backend validates the player before authenticated WebSocket actions.

Database credentials are supplied through environment variables.

Deployment-specific configuration is kept outside the source code.

Future Improvements

Additional word categories

Custom word lists

Player avatars

Player moderation

Kick/ban functionality

Spectator mode

Replay functionality

Multiple language support

Additional game modes

Author

Sushil Verma

B.Tech Computer Science Engineering

Live Application: https://skribbl-clone-2-gnt5.onrender.com

GitHub Repository: https://github.com/SushilVerma18/skribbl-clone
