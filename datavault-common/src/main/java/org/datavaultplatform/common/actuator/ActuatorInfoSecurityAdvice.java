package org.datavaultplatform.common.actuator;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@ControllerAdvice
public class ActuatorInfoSecurityAdvice extends BaseActuatorSecurityAdvice {

    @Override
    public Predicate<HttpServletRequest> getAcuatorUrlMatcher() {
        return req -> req.getRequestURI().startsWith("/actuator/info");
    }

    @Override
    public List<String> getKeysToKeep(){
        return List.of("app");
    }

    @Override
    public Object filter(Object fullInfo) {
        if (!(fullInfo instanceof Map<?, ?> fullInfoAsMap)) {
            return fullInfo;
        }
        // Create a "Safe" copy with only the bare minimum
        Map<String, Object> filteredInfo = new HashMap<>();
        for (String key : getKeysToKeep()) {
            filteredInfo.put(key, fullInfoAsMap.get(key));
        }
        return filteredInfo;
    }
}