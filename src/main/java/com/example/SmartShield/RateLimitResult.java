package com.example.SmartShield;

public class RateLimitResult {
    private final boolean isAllowed;
    private final long retryAfter; //ms
    private final int remaining;  //remaining requests

    public RateLimitResult(boolean isAllowed, long retryAfter, int remaining) {
        this.isAllowed = isAllowed;
        this.retryAfter = retryAfter;
        this.remaining = remaining;
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
}
