package org.datavaultplatform.broker.exceptions;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 01/07/2016
 * Time: 09:11
 */

@ControllerAdvice
public class GlobalExceptionHandler {

    // Note - Add methods for more specific Exceptions here.

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> defaultExceptionHandler(HttpServletRequest req, Exception e) throws Exception {
        // If the exception is annotated with @ResponseStatus rethrow it and let the framework handle it.
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
            throw e;

        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
