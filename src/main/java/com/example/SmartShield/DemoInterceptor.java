package com.example.SmartShield;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DemoInterceptor implements HandlerInterceptor {

    /**
     * Thread-safe map to store request counts per IP.Map is updated ,and it stores timestamps list for the requests
     * ConcurrentHashMap is used because multiple requests (threads)
     * can access and update this map simultaneously.
     */
    private static final Map<String , List<Long>> requestTimestamps=new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * preHandle() runs BEFORE the request reaches the controller.
     * It acts like a checkpoint/security gate:
     * We can allow or block the request here.
     * Returning true  -> request proceeds to controller
     * Returning false -> request is blocked immediately
     * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //Get client IP address(for tracking requests per user)
        String ipAddr= request.getRemoteAddr();

        //use to track when request came
        long now=System.currentTimeMillis();
        //Creating a timestamp list and storing request times for each ip to apply time based filtering
        requestTimestamps.putIfAbsent(ipAddr, Collections.synchronizedList(new ArrayList<>()));

        List<Long> timeStamps=requestTimestamps.get(ipAddr);

        /*
          Remove old requests , we only care about the requests in the last 60 seconds(Sliding Window approach)
         */
        long windowStart=now-60000;//60 secs window
        timeStamps.removeIf(time->time < windowStart);

        /*
          Simple rate limiting logic :
          If number of requests in last 60 sec from an IP >= 5 → block further requests
         */
        if(timeStamps.size()>=5){
            System.out.println("Blocked ip : "+ipAddr);
            response.setStatus(429);
            response.getWriter().write("Too many requests - try again later");

            //return false stops the request here itself
            return false;
        }

        //record current timestamp i.e. allow the request
        timeStamps.add(now);

        /*
         * Generating a unique request ID to track this request
         * across preHandle, postHandle, and afterCompletion.
         */
        String id = java.util.UUID.randomUUID().toString();
        request.setAttribute("requestId", id);

        System.out.println(id + " PRE");
        System.out.println("IP : "+ipAddr+"| Requests(last 60 secs) : "+timeStamps.size());
        return true;
    }

    /**
    * postHandle() runs AFTER controller execution
    * but before the response is sent to the client.
    */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

        String id = (String) request.getAttribute("requestId");
        System.out.println(id + " POST");
    }

    /**
     * afterCompletion() runs AFTER the complete request lifecycle
     * i.e. ,after response is sent to client.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        String id = (String) request.getAttribute("requestId");
        System.out.println(id + " AFTER + IP: " + request.getRemoteAddr());
    }
}
