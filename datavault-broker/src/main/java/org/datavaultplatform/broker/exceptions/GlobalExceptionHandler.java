package org.datavaultplatform.broker.exceptions;

import org.datavaultplatform.common.api.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Catches any Exceptions thrown by a Controller, or anything further down the stack, allowing us to return
 * a ResponseEntity that can be nicely formatted as Json by Jackson.
 *
 * User: Robin Taylor
 * Date: 01/07/2016
 * Time: 09:11
 */

@ControllerAdvice
public class GlobalExceptionHandler {

    // Note - Add methods for more specific Exceptions here.

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> defaultExceptionHandler(Exception ex) {
        //Make sure error is printing in the broker logs
        ex.printStackTrace();

        // If the exception is annotated with @ResponseStatus rethrow it and let the framework handle it.
//        if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null)
//            throw ex;

        String errorMessage = "The Broker threw an Error!";

        System.out.println("Sending ApiError to webapp...");

        ApiError apiError = new ApiError(errorMessage, ex);
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
