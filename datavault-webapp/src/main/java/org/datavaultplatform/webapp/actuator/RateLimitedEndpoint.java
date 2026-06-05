package org.datavaultplatform.webapp.actuator;

import org.datavaultplatform.webapp.config.ratelimited.RateLimitedProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Endpoint(id = "ratelimited")
@Component
public class RateLimitedEndpoint {
    
    final RateLimitedProperties rateLimitedProperties;
    
    public RateLimitedEndpoint(RateLimitedProperties rateLimitedProperties) {
        this.rateLimitedProperties = rateLimitedProperties;
    }
    
    @ReadOperation
    public RateLimitedInfoWrapper getActuatorEndpointInfo() {
        return new RateLimitedInfoWrapper(this.rateLimitedProperties);
        
    }
    
    public record RateLimitedInfoWrapper(RateLimitedProperties rateLimited) {
    }
}
