package com.example.SmartShield;

import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimiterService {

    private final Map<String , Deque<Long>> requestTimestamps=new java.util.concurrent.ConcurrentHashMap<>();


    public RateLimitResult checkRequest(String key,int maxRequests,long windowMs){

        long now=System.currentTimeMillis();
        long windowStart=now-windowMs;

        Deque<Long> timestamps=requestTimestamps.computeIfAbsent(key,k->new ConcurrentLinkedDeque<>());

        //remove old
        while(!timestamps.isEmpty() && timestamps.peekFirst()<windowStart){
            timestamps.pollFirst();
        }

        int currentSize=timestamps.size();

        if(currentSize>=maxRequests){
            long oldest=timestamps.peekFirst();
            long retryAfter=Math.max(0,(oldest+windowMs)-now);
            return new RateLimitResult(false,retryAfter,0);
        }

        timestamps.addLast(now);
        int remaining=maxRequests-timestamps.size();
        return new RateLimitResult(true,0,remaining);
    }
}
