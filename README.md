# SmartShield 🚀

SmartShield is a Spring Boot-based rate limiting system that protects APIs from excessive or abusive traffic using a sliding window algorithm.

## Features

* Annotation-based rate limiting using `@RateLimited`
* Sliding window algorithm (Deque)
* Per-IP + endpoint limiting
* Returns HTTP 429 (Too Many Requests)
* Custom headers: `Retry-After`, `RateLimit-Limit`, `RateLimit-Remaining`, `RateLimit-Reset`

## How it works

1. `HandlerInterceptor` intercepts incoming requests
2. Reads `@RateLimited` annotation from controller method
3. Delegates to `RateLimiterService`
4. Sliding window logic is applied
5. Request is allowed or blocked

## Example

```java
@RateLimited(maxRequests = 5, windowMs = 60000)
@GetMapping("/api/test")
public String test() {
    return "Protected endpoint";
}
```

## Tech Stack

* Java
* Spring Boot


