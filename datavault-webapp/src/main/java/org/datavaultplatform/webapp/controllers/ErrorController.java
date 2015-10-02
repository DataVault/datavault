package org.datavaultplatform.webapp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;

/**
 * Created by stuartlewis on 02/10/15.
 */
@Controller
class ErrorController {

    @RequestMapping("error")
    public String customError(HttpServletRequest request, HttpServletResponse response, Model model) {
        // Retrieve some useful information from the request
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
        String exceptionMessage = getExceptionMessage(throwable, statusCode);

        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (requestUri == null) {
            requestUri = "Unknown";
        }

        String message = MessageFormat.format("{0} returned for {1} with message {2}",
                statusCode, requestUri, exceptionMessage
        );

        model.addAttribute("message", message);
        return "error/error";
    }

    private String getExceptionMessage(Throwable throwable, Integer statusCode) {
        if (throwable != null) {
            String reason = throwable.getCause().getMessage();
            if (reason != null) return reason;
        }
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
        return httpStatus.getReasonPhrase();
    }
}