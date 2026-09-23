package com.example.skribbl.security;

import java.util.UUID;

public record PlayerSession(
        UUID playerId,
        String token
) {
}