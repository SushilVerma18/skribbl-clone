package com.example.skribbl.dto.request;
import com.example.skribbl.enums.WordMode;
import jakarta.validation.constraints.*;
public record CreateRoomRequest(
 @NotBlank @Size(min=2,max=20) String playerName,
 @NotNull Boolean isPrivate,
 @Min(2) @Max(20) int maxPlayers,
 @Min(2) @Max(10) int rounds,
 @Min(15) @Max(240) int drawTime,
 @Min(1) @Max(5) int wordCount,
 @Min(0) @Max(5) int hints,
 @NotNull WordMode wordMode) {}
