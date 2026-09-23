package com.example.skribbl.model;
import java.util.UUID;
public class PlayerState {
    public final UUID id; public final String name;
    public int score; public boolean ready=true; public boolean connected=true;
    public PlayerState(UUID id,String name){this.id=id;this.name=name;}
}
