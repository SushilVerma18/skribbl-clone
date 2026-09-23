package com.example.skribbl.entity;
import com.example.skribbl.enums.WordMode;
import jakarta.persistence.*;
@Entity
public class RoomSettings {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private int maxPlayers=8, rounds=3, drawTime=60, wordCount=3, hints=2;
    @Enumerated(EnumType.STRING) private WordMode wordMode=WordMode.NORMAL;
    public Long getId(){return id;} public int getMaxPlayers(){return maxPlayers;} public void setMaxPlayers(int v){maxPlayers=v;}
    public int getRounds(){return rounds;} public void setRounds(int v){rounds=v;} public int getDrawTime(){return drawTime;} public void setDrawTime(int v){drawTime=v;}
    public int getWordCount(){return wordCount;} public void setWordCount(int v){wordCount=v;} public int getHints(){return hints;} public void setHints(int v){hints=v;}
    public WordMode getWordMode(){return wordMode;} public void setWordMode(WordMode v){wordMode=v;}
}
