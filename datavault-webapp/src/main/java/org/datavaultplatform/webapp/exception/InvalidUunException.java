package org.datavaultplatform.webapp.exception;

public class InvalidUunException extends Exception {

    public InvalidUunException(String uun) {
        super("Invalid UUN: " + uun);
    }

    public InvalidUunException(String uun, Throwable t) {
        super("Invalid UUN: " + uun, t);
    }
}
