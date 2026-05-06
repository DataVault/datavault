package org.datavaultplatform.webapp.config.trace;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class BaseMdcFilter implements Filter {
    public static final String MDC_USER = "user";
    public static final String REQUEST_USER = "req-user";
    public static final String ANONYMOUS = "anonymous";

    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain filterChain)
            throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (request.getRequestURI().startsWith("/resources")) {
            filterChain.doFilter(request, response);
            return;
        }
        processFilterInternal(request, response, filterChain);
    }

    protected abstract void processFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException;
}
