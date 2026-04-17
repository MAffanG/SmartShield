package com.example.SmartShield;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Component
public class DemoInterceptor implements HandlerInterceptor {

    private static final Logger log= LoggerFactory.getLogger(DemoInterceptor.class);
    private final  RateLimitConfig config;
    private final RateLimiterService service;

    @Autowired
    public DemoInterceptor(RateLimitConfig config, RateLimiterService service){
        this.config = config;
        this.service=service;}

    /**
     * Thread-safe map to store request counts per IP.Map is updated ,and it stores timestamps list for the requests
     * ConcurrentHashMap is used because multiple requests (threads)
     * can access and update this map simultaneously.
     */
    // Using Deque for efficient sliding window: O(1) removal of oldest requests from the front instead of O(n) when we were using list
    /**
     * preHandle() runs BEFORE the request reaches the controller.
     * It acts like a checkpoint/security gate:
     * We can allow or block the request here.
     * Returning true  -> request proceeds to controller
     * Returning false -> request is blocked immediately
     * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /**
         * Identify client + endpoint
         * Combines IP and request path to track rate limiting per endpoint.
         */
        if(handler instanceof HandlerMethod method) {

                // Delegating rate-limiting logic to a dedicated service layer.
                // This keeps the interceptor lightweight and focuses only on request handling,
                // while the service encapsulates the core business logic for better modularity and reusability.
//                RateLimitResult result = service.checkRequest(key, maxRequests, window);
                RateLimitResult result = service.checkRequest(request, method);

                if (!result.isAllowed()) {

                    String ipAddr=request.getRemoteAddr();
                    String path=request.getRequestURI();
                    log.warn("rate_limit_blocked IP : {} Path : {} retry_after_sec : {}", ipAddr, path, result.getRetryAfter() / 1000);

                    response.setStatus(429);
                    response.setHeader("Retry-After", String.valueOf(Math.max(1, result.getRetryAfter() / 1000)));
                    response.setHeader("RateLimit-Limit", String.valueOf(result.getLimit()));
                    response.setHeader("RateLimit-Remaining", "0");
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
                //allowed
            if(result.getLimit()>0) {
                response.setHeader("RateLimit-Limit", String.valueOf(result.getLimit()));
                response.setHeader("RateLimit-Remaining", String.valueOf(result.getRemaining()));
            }
                log.info("rate_limit_allowed IP : {} Path : {}  Remaining : {}", request.getRemoteAddr(), request.getRequestURI(), result.getRemaining());
        }
        return true;
    }

    /**
    * postHandle() runs AFTER controller execution
    * but before the response is sent to the client.
    */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    /**
     * afterCompletion() runs AFTER the complete request lifecycle
     * i.e. ,after response is sent to client.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }
}
