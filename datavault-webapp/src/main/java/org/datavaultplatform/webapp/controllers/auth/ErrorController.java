package org.datavaultplatform.webapp.controllers.auth;

import java.util.Collections;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

/**
 * Created by stuartlewis on 02/10/15.
 */
@Controller
class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @RequestMapping("/error")
    public String customError(HttpServletRequest request, HttpServletResponse response, Model model) {
        // Retrieve some useful information from the request
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        String exceptionMessage = getExceptionMessage(throwable, statusCode, response);

        logger.error("----------");
        logger.error("An error occurred: {}", exceptionMessage, throwable);
        extraDebug(request);

        if (statusCode != null && statusCode == HttpStatus.FORBIDDEN.value()) {
            return "auth/denied";
        }

        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Unknown";
        }

        String message = MessageFormat.format("Error code {0} returned for {1} with message:<br/> {2}",
                                              statusCode, requestUri, exceptionMessage);

        model.addAttribute("message", message);
        return "error/error";
    }

    private void extraDebug(HttpServletRequest request) {
        Collections
            .list(request.getAttributeNames())
            .stream()
            .filter(Objects::nonNull)
            .filter(aName -> aName.startsWith("jakarta.servlet.error."))
            .forEach(aName -> logger.error("error attr [{}] -> [{}]", aName, request.getAttribute(aName)));
    }

    private String getExceptionMessage(Throwable throwable, Integer statusCode, HttpServletResponse response) {
        if (throwable != null) {
            StringWriter reason = new StringWriter();
            throwable.printStackTrace(new PrintWriter(reason));
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            if (reason != null) {
                return reason.toString();
            }
        }
        if(statusCode == null){
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        response.setStatus(statusCode);
        return httpStatus.getReasonPhrase();
    }
}
