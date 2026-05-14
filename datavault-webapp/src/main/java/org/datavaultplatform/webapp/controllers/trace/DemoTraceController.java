package org.datavaultplatform.webapp.controllers.trace;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.ExpressionAuthorizationDecision;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This MVC Controler is for the "database" profile (local development) only - it's used to demo the traceid show on the error page.
 * It will NOT be part of the production application.
 */
@Slf4j
@RestController
@Profile("database")
@RequestMapping("/demo/trace")
public class DemoTraceController implements DemoTraceControllerApi {

    @Autowired
    Tracer tracer;

    @RequestMapping("/oops")
    public void oops() {
        String traceId = tracer.currentSpan().context().traceId();
        String msg = "SimulatedError - oops traceId: [%s]".formatted(traceId);
        log.info(msg);
        throw new RuntimeException(msg);
    }

    @RequestMapping("/id")
    public String id() {
        String traceId = tracer.currentSpan().context().traceId();
        log.info("ACTUAL traceId[{}]", traceId);
        return traceId;
    }

    /**
     * The NO_SUCH_ROLE does not exist - so the @PreAuthorize should always fail with AccessDeniedException
     * an AccessDeniedException is thrown from within Spring Security - before SpringMVC Controller code
     */
    @RequestMapping(value={"/auth/fail/springsec", "/auth/fail"})
    @PreAuthorize("hasAuthority('NO_SUCH_ROLE_1')")
    public void authFail1() {
        String traceId = tracer.currentSpan().context().traceId();
        String msg = "SHOULD NOT GET HERE : traceId: [%s]".formatted(traceId);
        log.error(msg);
        throw new RuntimeException(msg);
    }

    /**
     * an AccessDeniedException is thrown from within the Spring MVC Controller code
     */
    @RequestMapping("/auth/fail/controller")
    public void authFail2() {
        SpelExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression("hasAuthority('NO_SUCH_ROLE_2')");

        ExpressionAuthorizationDecision decision = new ExpressionAuthorizationDecision(false, expression);

        String traceId = tracer.currentSpan().context().traceId();
        throw new AuthorizationDeniedException("Access Denied[%s]".formatted(traceId), decision);   
    }
}
