package com.example.skribbl.model;
import com.example.skribbl.enums.GamePhase;
import java.time.Instant;
import java.util.*;
public class GameState {
    public GamePhase phase=GamePhase.LOBBY;
    public int round,totalRounds,turnIndex;
    public UUID drawerId; public String word;
    public List<String> wordOptions=new ArrayList<>();
    public Instant roundEnd;
    public Instant hintStart;
    public final Set<UUID> guessed=new HashSet<>();
    public final List<UUID> turnOrder=new ArrayList<>();
}
