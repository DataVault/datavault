package org.datavaultplatform.webapp.controllers.auth;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.controllers.trace.BaseErrorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.text.MessageFormat;

@Slf4j
@Controller
@RequestMapping("/auth")
public class AuthController extends BaseErrorController implements AuthControllerApi {
    
    private final static String DEFAULT_LOGOUT_URL = "/auth/login?logout";
    
    private final String welcome;
    private final String logoutUrl;
    private final boolean outputTraceIdOnError;
    

    @Autowired
    public AuthController(
        @Value("${webapp.welcome}") String welcome,
        @Value("${webapp.logout.url}") String logoutUrl,
        @Value("${output.traceid.on.error:false}") boolean outputTraceIdOnError) {
        if (logoutUrl == null || logoutUrl.isEmpty()) {
            logoutUrl = AuthController.DEFAULT_LOGOUT_URL;
        }
        this.welcome = welcome;
        this.logoutUrl = logoutUrl;
        this.outputTraceIdOnError = outputTraceIdOnError;
    }

    @Override
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public String getLoginPage(@RequestParam(value = "error", required = false) boolean error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "security", required = false) String security,
                               ModelMap model) {

        model.put("success", "");
        model.put("error", "");
        model.put("welcome", welcome);
        
        if (logout != null) {
            // Logout
            model.put("success", "You are now logged out");
        }
        if (error) {
            // Login failed
            model.put("error", "Invalid username or password!");
        } else if (security != null) {
            // Permissions changed
            model.put("error", "You have been logged out for security reasons. Please log back in to continue");
        }
        
        return "auth/login";
    }

    @Override
    @GetMapping(value = "/logout")
    public String redirectToLogout(ModelMap model, HttpSession session) {

        session.invalidate();
        return "redirect:"+logoutUrl;
    }

    @Override
    @GetMapping(value = "/denied")
    public String getDeniedPage(HttpServletRequest request, HttpServletResponse response, Model model) {

        // Retrieve some useful information from the request
        Throwable throwable = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        HttpStatus httpStatus = getHttpStatus(statusCode, HttpStatus.OK);
        response.setStatus(httpStatus.value());

        return withinTraceContext(request, (traceId, spanId) -> {
            log.info("IN:traceId: {}, spanId: {}", traceId, spanId, throwable);
            extraDebug(request);

            String exceptionMessage = getExceptionMessage(throwable, httpStatus);

            if (throwable instanceof AuthorizationDeniedException adEx) {
                String authDeniedMessage = MessageFormat.format("Access Denied: {0}", adEx.getAuthorizationResult());
                log.error(authDeniedMessage);
            }

            String message = getMessage(requestUri, httpStatus, exceptionMessage, traceId, outputTraceIdOnError);
            model.addAttribute("message", message);

            log.info("OUT:traceId: {}, spanId: {}", traceId, spanId);

            return "auth/denied";
        });
    }

    @Override
    @GetMapping(value = "/confirmation", produces = MediaType.TEXT_HTML_VALUE)
    public String getConfirmationPage(ModelMap model) {

        model.put("logout", "");

        return "auth/confirmation";
    }

    @Override
    protected boolean showFullErrorMessageWhenNotDisplayingTraceId(){
        return false;
    }
}