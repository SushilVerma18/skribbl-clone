package com.example.skribbl.repository;
import com.example.skribbl.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PlayerRepository extends JpaRepository<Player,UUID> { List<Player> findByRoomId(UUID roomId); }
