package org.datavaultplatform.webapp.config.trace;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.datavaultplatform.common.util.MdcUtils;

import java.io.IOException;

/**
 * Restores the username in MDC context after the REQUEST dispatch has been processed, and we are dealing with ERROR or FORWARD dispatch (which can bypass SpringSecurityFilters).
 * @see MdcRequestFilter
 */
public class MdcRestorationFilter extends BaseMdcFilter {

    @Override
    protected void processFilterInternal(HttpServletRequest request,
                                         HttpServletResponse response,
                                         FilterChain filterChain) throws ServletException, IOException {

        if (request.getDispatcherType() != DispatcherType.REQUEST) {
            String user = (String) request.getAttribute(REQUEST_USER);
            String username = MdcUtils.getMdcUserName(user);
            MdcUtils.addUserNameToMdc(username);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MdcUtils.removeMdcUserName();
        }
    }
}
