package org.datavaultplatform.common.actuator;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@ControllerAdvice
public class ActuatorHealthSecurityAdvice extends BaseActuatorSecurityAdvice {
    
    @Override
    public Predicate<HttpServletRequest> getAcuatorUrlMatcher() {
        return req -> req.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    public List<String> getKeysToKeep(){
        return List.of("status");
    }

    @Override
    public Object filter(Object fullInfo) {
        if( !(fullInfo instanceof SystemHealth systemHealth)){
            return fullInfo;
        }
        Map<String,Object> result = new HashMap<>();
        result.put("status", systemHealth.getStatus().getCode());
        return result;
    }
}