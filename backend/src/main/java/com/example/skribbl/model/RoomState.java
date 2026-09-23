package com.example.skribbl.model;
import java.util.*;
public class RoomState {
    public final UUID roomId; public final Map<UUID,PlayerState> players=new LinkedHashMap<>(); public final GameState game=new GameState();
    public RoomState(UUID id,int rounds){roomId=id;game.totalRounds=rounds;}
}
