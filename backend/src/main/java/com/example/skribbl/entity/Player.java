package com.example.skribbl.entity;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="players")
public class Player {
    @Id private UUID id;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private UUID roomId;
    private int score;
    private boolean ready;
    private boolean connected=true;
    public Player() {}
    public Player(UUID id,String name,UUID roomId){this.id=id;this.name=name;this.roomId=roomId;}
    public UUID getId(){return id;} public String getName(){return name;} public UUID getRoomId(){return roomId;}
    public int getScore(){return score;} public void setScore(int v){score=v;}
    public boolean isReady(){return ready;} public void setReady(boolean v){ready=v;}
    public boolean isConnected(){return connected;} public void setConnected(boolean v){connected=v;}
}
