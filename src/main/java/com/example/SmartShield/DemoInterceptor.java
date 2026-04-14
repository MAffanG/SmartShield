package com.example.SmartShield;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class DemoInterceptor implements HandlerInterceptor {

    private static final Logger log= LoggerFactory.getLogger(DemoInterceptor.class);
    private final  RateLimitConfig config;
    private final RateLimiterService service;

    @Autowired
    public DemoInterceptor(RateLimitConfig config, RateLimiterService service){
        this.config = config;
        this.service=service;}


    //private static final int MAX_REQUESTS = 5;
    //private static final long TIME_WINDOW = 60000;
    /**
     * Thread-safe map to store request counts per IP.Map is updated ,and it stores timestamps list for the requests
     * ConcurrentHashMap is used because multiple requests (threads)
     * can access and update this map simultaneously.
     */

  //  private static final Map<String , List<Long>> requestTimestamps=new java.util.concurrent.ConcurrentHashMap<>();

    // Using Deque for efficient sliding window: O(1) removal of oldest requests from the front instead of O(n) when we were using list
    //private static final Map<String , Deque<Long>> requestTimestamps=new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * preHandle() runs BEFORE the request reaches the controller.
     * It acts like a checkpoint/security gate:
     * We can allow or block the request here.
     * Returning true  -> request proceeds to controller
     * Returning false -> request is blocked immediately
     * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String path=request.getRequestURI();

        //Get client IP address(for tracking requests per user)
        String ipAddr= request.getRemoteAddr();

        int maxRequests=path.contains("/login") ?
                config.getLogin().getMaxRequests() : config.getDefaultLimit().getMaxRequests();

//      //dynamic window size
        long window=path.contains("/login") ?
                config.getLogin().getWindowMs() : config.getDefaultLimit().getWindowMs();


        /**
         * Identify client + endpoint
         * Combines IP and request path to track rate limiting per endpoint.
         */
        String key=ipAddr+":"+path;

        //use to track when request came

       /* long now=System.currentTimeMillis();*/

        //Creating a timestamp list and storing request times for each ip to apply time based filtering
        //requestTimestamps.putIfAbsent(key, Collections.synchronizedList((List<Object>) new ArrayList<>()));
        /* Rare race condition might occur
        requestTimestamps.putIfAbsent(key, new ConcurrentLinkedDeque<>());
        Deque<Long> timeStamps=requestTimestamps.get(key);*/
       /* Deque<Long> timeStamps=requestTimestamps.computeIfAbsent(key,k -> new ConcurrentLinkedDeque<>());*/

        /*
          Remove old requests , we only care about the requests in the last 60 seconds(Sliding Window approach)
         */
        /*long windowStart=now-window;

        while(!timeStamps.isEmpty() && timeStamps.peekFirst()<windowStart){
            timeStamps.pollFirst();
        }*/
        //timeStamps.removeIf(time->time < windowStart);

        //int maxRequest=path.contains("/login") ? 3 : 5;

        /*
          Simple rate limiting logic :
          If number of requests in last 60 sec from an IP >= maxRequest → block further requests
         */


        // Delegating rate-limiting logic to a dedicated service layer.
        // This keeps the interceptor lightweight and focuses only on request handling,
        // while the service encapsulates the core business logic for better modularity and reusability.
//        boolean allowed=service.allowRequest(key,maxRequests,window);
        RateLimitResult result=service.checkRequest(key,maxRequests,window);

//        if(timeStamps.size()>=maxRequests){

          /*  long oldest=timeStamps.peekFirst();

            long retryAfter = Math.max(0, (oldest + window) - now);*/ //calculate retry time based on oldest request.
            //long retryAfter=(oldest+window) - now;

          if(!result.isAllowed()){
            //System.out.println("[Blocked] IP : "+ipAddr+"\t| Path : "+path+"\t| Limit : "+timeStamps.size()+" exceeded"+"\t Retry : "+retryAfter/1000+" secs.");
//            log.warn("[Blocked] IP : {} Path : {} Limit : {} exceeded  Retry : {} secs",ipAddr,path,timeStamps.size(),retryAfter/1000);
            log.warn("[Blocked] IP : {} Path : {} Retry : {} secs",ipAddr,path,result.getRetryAfter()/1000);

            response.setStatus(429);
           // response.setHeader("Retry-After", String.valueOf(retryAfter / 1000));
            response.setHeader("Retry-After", String.valueOf(Math.max(1,result.getRetryAfter()/1000)));
            response.setHeader("RateLimit-Limit", String.valueOf(maxRequests));
            response.setHeader("RateLimit-Remaining", "0");
            //response.setHeader("RateLimit-Reset", String.valueOf((oldest + window) / 1000));
            response.setHeader("RateLimit-Reset", String.valueOf((System.currentTimeMillis() + result.getRetryAfter()) / 1000));
            response.setContentType("application/json");
            String jsonResponse = "{"
                    + "\"error\": \"Too Many Requests\","
                    + "\"message\": \"Rate limit exceeded. You shall not pass (for now).\","
                    + "\"status\": 429,"
                    + "\"timestamp\": " + System.currentTimeMillis()
                    + "}";
            response.getWriter().write(jsonResponse);

            //return false stops the request here itself
            return false;
        }

        //record current timestamp i.e. allow the request
        //timeStamps.addLast(now);

        /*
         * Generating a unique request ID to track this request
         * across preHandle, postHandle, and afterCompletion.
         */
        /*String id = java.util.UUID.randomUUID().toString();
        request.setAttribute("requestId", id);

        System.out.println(id + " PRE");*/

        //System.out.println("[Allowed] IP : "+ipAddr+"\t| Path : "+path+"\t| Requests(last 60 secs) : "+"\t| "+timeStamps.size()+"/"+maxRequests);

        response.setHeader("RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("RateLimit-Remaining", String.valueOf(result.getRemaining()));
      //  log.info("[Allowed] IP : {} Path : {} Requests(last 60 secs) : {}/{}",ipAddr,path,timeStamps.size(),maxRequests);
        log.info("[Allowed] IP : {} Path : {}  Remaining : {}",ipAddr,path,result.getRemaining());

        return true;
    }

    /**
    * postHandle() runs AFTER controller execution
    * but before the response is sent to the client.
    */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

       /* String id = (String) request.getAttribute("requestId");
        System.out.println(id + " POST");*/
    }

    /**
     * afterCompletion() runs AFTER the complete request lifecycle
     * i.e. ,after response is sent to client.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        /*String id = (String) request.getAttribute("requestId");
        System.out.println(id + " AFTER + IP: " + request.getRemoteAddr());*/
    }
}
