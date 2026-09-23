package com.example.skribbl.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WordServiceTest {
 @Test void matchingNormalizesCaseAndWhitespace(){
   WordService s=new WordService(null);
   assertTrue(s.matches("  Elephant ","elephant"));
   assertFalse(s.matches("cat","dog"));
 }
}
