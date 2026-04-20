package com.example.SmartShield;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimiterService {

    private final ClientIpResolver clientIpResolver;
    public RateLimiterService(ClientIpResolver clientIpResolver){
        this.clientIpResolver=clientIpResolver;
    }

    private final Map<String , Deque<Long>> requestTimestamps=new java.util.concurrent.ConcurrentHashMap<>();


    public RateLimitResult checkRequest(HttpServletRequest request, HandlerMethod handlerMethod){

        //Read annotation
        RateLimited rateLimited=handlerMethod.getMethodAnnotation(RateLimited.class);

        //No annotation  - allow request
        if(rateLimited==null){
            return RateLimitResult.allowed();
        }

        int maxRequests= rateLimited.maxRequests();
        long windowMs=rateLimited.windowMs();

        long now=System.currentTimeMillis();
        long windowStart=now-windowMs;

        //Build key
//        String ip=request.getRemoteAddr();
        String ip=clientIpResolver.getClientIp(request);
        String path=request.getRequestURI();
        String key=ip+":"+path;
        Deque<Long> timestamps=requestTimestamps.computeIfAbsent(key,k->new ConcurrentLinkedDeque<>());

        //remove old requests ie.expired
        while(!timestamps.isEmpty() && timestamps.peekFirst()<windowStart){
            timestamps.pollFirst();
        }

        int currentSize=timestamps.size();

        if(currentSize>=maxRequests){
            long oldest=timestamps.peekFirst();
            long retryAfter=Math.max(0,(oldest+windowMs)-now);
            return RateLimitResult.blocked(retryAfter,maxRequests,0);
        }

        timestamps.addLast(now);
        int remaining=maxRequests-timestamps.size();
        return RateLimitResult.allowed(maxRequests, remaining);
    }
}
