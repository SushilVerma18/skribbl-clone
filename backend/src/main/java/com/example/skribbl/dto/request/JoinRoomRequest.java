package com.example.skribbl.dto.request;
import jakarta.validation.constraints.*;
public record JoinRoomRequest(@NotBlank @Size(min=2,max=20) String playerName) {}
