package org.datavaultplatform.webapp.config.ratelimited;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class RateLimitExceededEvent extends ApplicationEvent {
    private final String username;
    private final String requestUri;

    public RateLimitExceededEvent(Object source, Clock clock, String username, String requestUri ) {
        super(source, clock);
        this.username = username;
        this.requestUri = requestUri;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getUsername() {
        return username;
    }
}