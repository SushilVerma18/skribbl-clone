package com.example.skribbl.websocket;

import com.example.skribbl.service.GameService;
import com.example.skribbl.service.RoomService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
public class GameWebSocketController {

    private final GameService game;
    private final RoomService rooms;

    public GameWebSocketController(
            GameService game,
            RoomService rooms
    ) {
        this.game = game;
        this.rooms = rooms;
    }

    private UUID playerId(Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new SecurityException(
                    "WebSocket player is not authenticated"
            );
        }

        return UUID.fromString(principal.getName());
    }

    @MessageMapping("/room/{code}/draw")
    public void draw(
            @DestinationVariable String code,
            @Payload Map<String, Object> p,
            Principal principal
    ) {
        UUID id = playerId(principal);

        rooms.requirePlayer(code, id);

        // playerId from the client is deliberately ignored.
        p.remove("playerId");

        game.draw(code, id, p);
    }

    @MessageMapping("/room/{code}/choose-word")
    public void choose(
            @DestinationVariable String code,
            @Payload Map<String, String> p,
            Principal principal
    ) {
        UUID id = playerId(principal);

        rooms.requirePlayer(code, id);

        game.chooseWord(
                code,
                id,
                p.get("word")
        );
    }

    @MessageMapping("/room/{code}/guess")
    public void guess(
            @DestinationVariable String code,
            @Payload Map<String, String> p,
            Principal principal
    ) {
        UUID id = playerId(principal);

        rooms.requirePlayer(code, id);

        game.guess(
                code,
                id,
                p.get("text")
        );
    }

    @MessageMapping("/room/{code}/canvas")
    public void canvas(
            @DestinationVariable String code,
            @Payload Map<String, String> p,
            Principal principal
    ) {
        UUID id = playerId(principal);

        rooms.requirePlayer(code, id);

        game.canvasCommand(
                code,
                id,
                p.getOrDefault(
                        "type",
                        "CANVAS_CLEAR"
                )
        );
    }

    @MessageMapping("/room/{code}/state")
    public void state(
            @DestinationVariable String code,
            Principal principal
    ) {
        UUID id = playerId(principal);

        rooms.requirePlayer(code, id);

        game.sendCurrentState(code);
    }
}