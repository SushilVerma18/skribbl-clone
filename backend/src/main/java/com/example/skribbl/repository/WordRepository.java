package com.example.skribbl.repository;
import com.example.skribbl.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WordRepository extends JpaRepository<Word,Long> {}
