package org.datavaultplatform.webapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

/**
 * Created by stuartlewis on 02/10/15.
 */
@Controller
class ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @RequestMapping("error")
    public String customError(HttpServletRequest request, HttpServletResponse response, Model model) {
        // Retrieve some useful information from the request
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        String exceptionMessage = getExceptionMessage(throwable, statusCode, response);

        logger.error("An error occurred: {}", exceptionMessage, throwable);

        if (statusCode == 403) {
            return "auth/denied";
        }

        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Unknown";
        }

        String message = MessageFormat.format("Error code {0} returned for {1} with message:<br/> {2}",
                                              statusCode, requestUri, exceptionMessage);

        model.addAttribute("message", message);
        return "error/error";
    }

    private String getExceptionMessage(Throwable throwable, Integer statusCode, HttpServletResponse response) {
        if (throwable != null) {
            StringWriter reason = new StringWriter();
            throwable.printStackTrace(new PrintWriter(reason));
            response.setStatus(500);
            if (reason != null) return reason.toString();
        }
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        response.setStatus(statusCode);
        return httpStatus.getReasonPhrase();
    }
}