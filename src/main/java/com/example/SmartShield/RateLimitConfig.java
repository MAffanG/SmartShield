package com.example.SmartShield;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitConfig {

    private Limit defaultLimit;
    private Limit login;


    public static class Limit{

        private int maxRequests;
        private long windowMs;

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public long getWindowMs() {
            return windowMs;
        }

        public void setWindowMs(long windowMs) {
            this.windowMs = windowMs;
        }
    }

    public Limit getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(Limit defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public Limit getLogin() {
        return login;
    }

    public void setLogin(Limit login) {
        this.login = login;
    }
}
