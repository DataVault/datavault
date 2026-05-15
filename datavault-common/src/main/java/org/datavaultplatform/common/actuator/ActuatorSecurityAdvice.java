package org.datavaultplatform.common.actuator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
@ControllerAdvice
public class ActuatorSecurityAdvice extends BaseActuatorSecurityAdvice {

    public static final List<String> ALLOWED = List.of("/actuator", "/actuator/");
    public static final String LINKS = "_links";
    
    @Override
    public Predicate<HttpServletRequest> getAcuatorUrlMatcher() {
        return request -> {
            boolean allowed =  ALLOWED.contains(request.getRequestURI());
            return allowed;
        };
    }

    @Override
    public List<String> getKeysToKeep(){
        return List.of("self", "heath", "info");
    }

    @Override
    public Object filter(Object fullInfo) {
        if (!(fullInfo instanceof Map<?, ?> fullInfoAsMap)) {
            return fullInfo;
        }
        Map<String, Link> links = (Map<String, Link>) fullInfoAsMap.get(LINKS);

        LinkedHashMap<String, Link> filteredLinks = new LinkedHashMap<>();
        for (String key : getKeysToKeep()) {
            Link link = links.get(key);
            if (link != null) {
                filteredLinks.put(key, link);
            }
        }
        Map<String, Object> filteredInfo = new HashMap<>();
        filteredInfo.put(LINKS, filteredLinks);
        return filteredInfo;
    }
}