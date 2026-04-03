package com.example.SmartShield;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public class DemoInterceptor implements HandlerInterceptor {

    /**
     * Thread-safe map to store request counts per IP.
     * ConcurrentHashMap is used because multiple requests (threads)
     * can access and update this map simultaneously.
     */
    private static final Map<String ,Integer> requestCounts=new java.util.concurrent.ConcurrentHashMap<>();

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

        requestCounts.put(ipAddr,requestCounts.getOrDefault(ipAddr,0)+1);

        /*
          Simple rate limiting logic :
          If a single IP makes more than 5 requests,block further requests
         */
        if(requestCounts.get(ipAddr)>5){
            System.out.println("Blocked ip : "+ipAddr);
            response.setStatus(429);
            response.getWriter().write("Too many requests");

            //return false stops the request here itself
            return false;
        }

        /*
         * Generating a unique request ID to track this request
         * across preHandle, postHandle, and afterCompletion.
         */
        String id = java.util.UUID.randomUUID().toString();
        request.setAttribute("requestId", id);

        System.out.println(id + " PRE");
        System.out.println("IP : "+ipAddr+", Request Count : "+requestCounts.get(ipAddr));

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
