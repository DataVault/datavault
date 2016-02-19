package org.datavaultplatform.broker.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * User: Robin Taylor
 * Date: 17/02/2016
 * Time: 11:49
 */

public class RestAuthenticationException extends AuthenticationException {

    /**
     * Constructs a <code>RestAuthenticationException</code> with the specified
     * message.
     *
     * @param msg the detail message
     */
    public RestAuthenticationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an <code>RestAuthenticationException</code> with the specified
     * message and root cause.
     *
     * @param msg the detail message
     * @param t root cause
     */
    public RestAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }
}


