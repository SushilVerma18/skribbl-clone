package com.example.skribbl.dto.response;

import com.example.skribbl.enums.*;
import java.util.*;

public record RoomResponse(
        UUID roomId,
        String roomCode,
        RoomVisibility visibility,
        RoomStatus status,
        UUID hostId,
        String playerToken,
        Object settings,
        List<Map<String, Object>> players
) {
}