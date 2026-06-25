package org.datavaultplatform.webapp.actuator;

import org.datavaultplatform.webapp.config.ratelimited.RateLimitedProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "ratelimited")
public class RateLimitedEndpoint {
    
    final RateLimitedProperties rateLimitedProperties;
    
    public RateLimitedEndpoint(RateLimitedProperties rateLimitedProperties) {
        this.rateLimitedProperties = rateLimitedProperties;
    }
    
    @ReadOperation
    public RateLimitedProperties getActuatorEndpointInfo() {
        return this.rateLimitedProperties;
    }
    
}
