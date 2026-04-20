package com.example.SmartShield;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ClientIpResolver {

    public String getClientIp(HttpServletRequest request){
        String xfHeader=request.getHeader("X-Forwarded-For");

        if(xfHeader!=null && !xfHeader.isEmpty()){
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
/**
 * Some info on why I am using this
 * Resolves the real client IP address for logging, rate limiting, and security checks.
 *
 * In real-world deployments, the application is often behind one or more proxies
 * (e.g., load balancers, reverse proxies, CDNs like Nginx, AWS ALB, Cloudflare).
 *
 * In such cases:
 * - request.getRemoteAddr() returns the IP of the last proxy in the chain (not the actual client)
 * - "X-Forwarded-For" may contain the original client IP and proxy chain
 *
 * Therefore, this method attempts to extract the first IP from X-Forwarded-For
 * when available, otherwise falls back to getRemoteAddr().
 *
 * ⚠ Important:
 * X-Forwarded-For can be spoofed if requests do not pass through a trusted proxy.
 * In production systems, this header should only be trusted when the request
 * originates from known proxy/load balancer IP ranges.
 */