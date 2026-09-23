package com.example.skribbl.entity;
import com.example.skribbl.enums.*;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="rooms")
public class Room {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(nullable=false,unique=true,length=8) private String code;
    @Enumerated(EnumType.STRING) private RoomVisibility visibility;
    @Enumerated(EnumType.STRING) private RoomStatus status=RoomStatus.LOBBY;
    @Column(nullable=false) private UUID hostPlayerId;
    @OneToOne(cascade=CascadeType.ALL,orphanRemoval=true) private RoomSettings settings;
    public UUID getId(){return id;} public String getCode(){return code;} public void setCode(String v){code=v;}
    public RoomVisibility getVisibility(){return visibility;} public void setVisibility(RoomVisibility v){visibility=v;}
    public RoomStatus getStatus(){return status;} public void setStatus(RoomStatus v){status=v;}
    public UUID getHostPlayerId(){return hostPlayerId;} public void setHostPlayerId(UUID v){hostPlayerId=v;}
    public RoomSettings getSettings(){return settings;} public void setSettings(RoomSettings v){settings=v;}
}
