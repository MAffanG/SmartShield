package com.example.SmartShield;

public class RateLimitResult {
    private final boolean isAllowed;
    private final long retryAfter; //ms
    private final int remaining;  //remaining requests
    private final int limit;

    public RateLimitResult(boolean isAllowed, long retryAfter,int limit, int remaining) {
        this.isAllowed = isAllowed;
        this.retryAfter = retryAfter;
        this.remaining = remaining;
        this.limit=limit;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public long getRetryAfter() {
        return retryAfter;
    }

    public int getRemaining() {
        return remaining;
    }

    public int getLimit() {
        return limit;
    }

    public static RateLimitResult allowed(){
        return new RateLimitResult(true,0,0,0);
    }

    public static RateLimitResult allowed(int limit,int remaining){
        return new RateLimitResult(true,0,limit,remaining);
    }

    public static RateLimitResult blocked(long retryAfter,int limit,int remaining){
        return new RateLimitResult(false,retryAfter,limit,remaining);
    }
}
