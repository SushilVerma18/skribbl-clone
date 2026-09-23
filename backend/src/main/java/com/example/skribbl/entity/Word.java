package com.example.skribbl.entity;
import jakarta.persistence.*;
@Entity @Table(name="words")
public class Word {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true) private String text;
    @Column(nullable=false) private String category;
    public Long getId(){return id;} public String getText(){return text;} public void setText(String v){text=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
}
