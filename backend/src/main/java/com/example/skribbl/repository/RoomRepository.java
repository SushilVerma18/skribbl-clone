package com.example.skribbl.repository;
import com.example.skribbl.entity.Room;
import com.example.skribbl.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface RoomRepository extends JpaRepository<Room,UUID> {
    Optional<Room> findByCodeIgnoreCase(String code);
    List<Room> findByVisibilityAndStatus(RoomVisibility visibility,RoomStatus status);
}
