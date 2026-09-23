package com.example.skribbl.service;
import org.springframework.stereotype.Service;
@Service
public class ScoringService {
    public int points(long remainingSeconds,int drawTime,int rank){
        int speed=(int)Math.max(20,Math.min(100,remainingSeconds*100/Math.max(1,drawTime)));
        return 100+speed+Math.max(0,40-(rank-1)*10);
    }
}
