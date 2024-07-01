package org.datavaultplatform.webapp.authentication.shib;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import jakarta.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:27
 */

public class ShibWebAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    private String firstnameRequestHeader;
    private String lastnameRequestHeader;
    private String emailRequestHeader;

    public String getFirstnameRequestHeader() {
        return firstnameRequestHeader;
    }

    public void setFirstnameRequestHeader(String firstnameRequestHeader) {
        this.firstnameRequestHeader = firstnameRequestHeader;
    }

    public String getLastnameRequestHeader() {
        return lastnameRequestHeader;
    }

    public void setLastnameRequestHeader(String lastnameRequestHeader) {
        this.lastnameRequestHeader = lastnameRequestHeader;
    }

    public String getEmailRequestHeader() {
        return emailRequestHeader;
    }

    public void setEmailRequestHeader(String emailRequestHeader) {
        this.emailRequestHeader = emailRequestHeader;
    }

    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new ShibWebAuthenticationDetails(context, getFirstnameRequestHeader(), getLastnameRequestHeader(), getEmailRequestHeader());
    }

}
