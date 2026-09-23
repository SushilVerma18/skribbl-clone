package com.example.skribbl.exception;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(RoomNotFoundException.class) ResponseEntity<?> notFound(RoomNotFoundException e){return ResponseEntity.status(404).body(Map.of("message",e.getMessage()));}
 @ExceptionHandler({RoomFullException.class,InvalidGameStateException.class,NotRoomHostException.class}) ResponseEntity<?> bad(RuntimeException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}
}
