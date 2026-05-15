package org.datavaultplatform.webapp.config.trace;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.MdcUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * This class takes the username of the logged-in user from spring-security Authentication object and places in the logback MDC so that it can be used in the log format under <code>%X{user:-}</code>
 * <br/>
 * e.g. <code>%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %clr([trace=%X{traceId:-} span=%X{spanId:-} user=%X{user:-}]){yellow} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%rEx}</code>
 * It also puts the username into the request so that when there is either an ERROR Dispatch or FORWARD Dispatch ( spring-security filters can be by-passed) - that the ErrorController or AutheConrtoller can get the username from the request.
 * @see MdcRestorationFilter
 */
@Slf4j
public class MdcRequestFilter extends BaseMdcFilter {
    @Override
    protected void processFilterInternal(HttpServletRequest request,
                                         HttpServletResponse response,
                                         FilterChain filterChain)
            throws ServletException, IOException {

        log.info("dispatch type [{}]", request.getDispatcherType().name());

        // Only populate MDC on the original REQUEST
        if (request.getDispatcherType() == DispatcherType.REQUEST) {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            String username = null;
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }
            username = MdcUtils.getMdcUserName(username);
            MdcUtils.addUserNameToMdc(username);

            // Store in request so it survives ERROR dispatch
            request.setAttribute(REQUEST_USER, username);
            log.info("request[req-user] now [{}]", username);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MdcUtils.removeMdcUserName();
        }
    }
}