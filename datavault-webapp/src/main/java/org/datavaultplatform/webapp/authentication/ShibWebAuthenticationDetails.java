package org.datavaultplatform.webapp.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:23
 */

public class ShibWebAuthenticationDetails extends WebAuthenticationDetails {

    private String name;
    private String email;

    public ShibWebAuthenticationDetails(HttpServletRequest request, String nameRequestHeader, String emailRequestHeader) {
        super(request);
        setName(request.getHeader(nameRequestHeader));
        setEmail(request.getHeader(emailRequestHeader));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

















}
