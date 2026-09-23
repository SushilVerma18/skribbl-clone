package com.example.skribbl.controller;

import com.example.skribbl.dto.request.*;
import com.example.skribbl.dto.response.RoomResponse;
import com.example.skribbl.security.PlayerSessionService;
import com.example.skribbl.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService rooms;
    private final GameService games;
    private final PlayerSessionService sessions;

    public RoomController(
            RoomService rooms,
            GameService games,
            PlayerSessionService sessions
    ) {
        this.rooms = rooms;
        this.games = games;
        this.sessions = sessions;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> create(
            @Valid @RequestBody CreateRoomRequest q
    ) {
        return ResponseEntity
                .status(201)
                .body(rooms.create(q));
    }

    @GetMapping("/{code}")
    public RoomResponse get(
            @PathVariable String code
    ) {
        return rooms.get(code);
    }

    @PostMapping("/{code}/join")
    public RoomResponse join(
            @PathVariable String code,
            @Valid @RequestBody JoinRoomRequest q
    ) {
        return rooms.join(code, q);
    }

    @PostMapping("/{code}/ready")
    public RoomResponse ready(
            @PathVariable String code,
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ReadyRequest q
    ) {
        UUID playerId = authenticate(authorization);

        rooms.setReady(
                code,
                playerId,
                q.ready()
        );

        return rooms.get(code);
    }

    @PostMapping("/{code}/start")
    public RoomResponse start(
            @PathVariable String code,
            @RequestHeader("Authorization") String authorization
    ) {
        UUID playerId = authenticate(authorization);

        games.start(
                code,
                playerId
        );

        return rooms.get(code);
    }

    @GetMapping("/public")
    public List<Map<String, Object>> publicRooms() {
        return rooms.publicRooms()
                .stream()
                .map(r -> Map.<String, Object>of(
                        "roomCode", r.getCode(),
                        "maxPlayers", r.getSettings().getMaxPlayers(),
                        "rounds", r.getSettings().getRounds()
                ))
                .toList();
    }

    private UUID authenticate(String authorization) {

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {
            throw new SecurityException(
                    "Missing or invalid authorization token"
            );
        }

        String token = authorization.substring(7);

        return sessions.authenticate(token);
    }
}