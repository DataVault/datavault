package org.datavaultplatform.webapp.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:27
 */

public class ShibWebAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    // Can be overridden in Spring config
    private String nameRequestHeader = "cn";
    private String emailRequestHeader = "email";

    public String getNameRequestHeader() {
        return nameRequestHeader;
    }

    public void setNameRequestHeader(String nameRequestHeader) {
        this.nameRequestHeader = nameRequestHeader;
    }

    public String getEmailRequestHeader() {
        return emailRequestHeader;
    }

    public void setEmailRequestHeader(String emailRequestHeader) {
        this.emailRequestHeader = emailRequestHeader;
    }

    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new ShibWebAuthenticationDetails(context, getNameRequestHeader(), getEmailRequestHeader());
    }

}
