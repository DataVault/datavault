package org.datavaultplatform.common.actuator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;
import java.util.function.Predicate;

@ControllerAdvice
@Slf4j
public abstract class BaseActuatorSecurityAdvice implements ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
        if (!getAcuatorUrlMatcher().test(req)) {
            return body;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isActuatorUser = auth != null && 
                auth.isAuthenticated() && 
                auth.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_ACTUATOR"));

        if (!isActuatorUser) {
            log.info("CLASS [{}]", this.getClass().getName());
            Object filtered = this.filter(body);
            return filtered;
        } else {
            return body;
        }
    }

    public abstract Predicate<HttpServletRequest> getAcuatorUrlMatcher();

    public abstract List<String> getKeysToKeep();

    public abstract Object filter(Object fullInfo);
}