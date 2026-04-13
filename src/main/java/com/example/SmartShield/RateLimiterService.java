package com.example.SmartShield;

import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimiterService {

    private final Map<String , Deque<Long>> requestTimestamps=new java.util.concurrent.ConcurrentHashMap<>();


    public boolean allowRequest(String key,int maxRequests,long windowMs){

        long now=System.currentTimeMillis();
        long windowStart=now-windowMs;

        Deque<Long> timestamps=requestTimestamps.computeIfAbsent(key,k->new ConcurrentLinkedDeque<>());

        //remove old
        while(!timestamps.isEmpty() && timestamps.peekFirst()<windowStart){
            timestamps.pollFirst();
        }

        if(timestamps.size()>=maxRequests){
            return false;
        }

        timestamps.addLast(now);

        return true;
    }
}
