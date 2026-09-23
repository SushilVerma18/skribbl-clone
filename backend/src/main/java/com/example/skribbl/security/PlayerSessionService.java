package com.example.skribbl.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerSessionService {

    private final Map<String, UUID> sessions =
            new ConcurrentHashMap<>();

    private final SecureRandom random =
            new SecureRandom();

    public PlayerSession create(UUID playerId) {

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        String token =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

        sessions.put(token, playerId);

        return new PlayerSession(playerId, token);
    }

    public UUID authenticate(String token) {

        if (token == null || token.isBlank()) {
            throw new SecurityException("Missing player session token");
        }

        UUID playerId = sessions.get(token);

        if (playerId == null) {
            throw new SecurityException("Invalid player session token");
        }

        return playerId;
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}