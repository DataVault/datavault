package org.datavaultplatform.webapp.controllers.auth;

import jakarta.servlet.RequestDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.controllers.trace.BaseErrorController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Created by stuartlewis on 02/10/15.
 */
@Controller
@Slf4j
public class ErrorController extends BaseErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private final boolean outputTraceIdOnError;

    public ErrorController(@Value("${output.traceid.on.error:false}") boolean outputTraceIdOnError) {
        this.outputTraceIdOnError = outputTraceIdOnError;
    }

    @RequestMapping("/error")
    public String customError(HttpServletRequest request, HttpServletResponse response, Model model) {

        // Retrieve some useful information from the request
        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        HttpStatus httpStatus = getHttpStatus(statusCode, HttpStatus.INTERNAL_SERVER_ERROR);
        response.setStatus(httpStatus.value());

        if (httpStatus == HttpStatus.NOT_FOUND && requestUri.startsWith("/resources/")) {
            return null; //this bypasses templating and returns a 404 with no-content to the browser.
        }

        // this handles the case where a SpringMVC controller threw AccessDeniedException directly
        if (httpStatus == HttpStatus.FORBIDDEN) {
            return "forward:auth/denied";
        }

        return withinTraceContext(request, (traceId, spanId) -> {
            log.info("IN:traceId: {}, spanId: {}", traceId, spanId, throwable);
            extraDebug(request);

            String exceptionMessage = getExceptionMessage(throwable, httpStatus);

            String message = getMessage(requestUri, httpStatus, exceptionMessage, traceId, outputTraceIdOnError);
            model.addAttribute("message", message);

            log.info("OUT:traceId: {}, spanId: {}", traceId, spanId);
            return "error/error";
        });
    }

    @Override
    protected boolean showFullErrorMessageWhenNotDisplayingTraceId(){
        return true;
    }
}
