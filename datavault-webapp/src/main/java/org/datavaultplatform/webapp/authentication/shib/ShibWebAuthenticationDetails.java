package org.datavaultplatform.webapp.authentication.shib;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:23
 */

public class ShibWebAuthenticationDetails extends WebAuthenticationDetails {

    private String firstname;
    private String lastname;
    private String email;

    public ShibWebAuthenticationDetails(HttpServletRequest request, String firstnameRequestHeader, String lastnameRequestHeader, String emailRequestHeader) {
        super(request);
        setFirstname(request.getHeader(firstnameRequestHeader));
        setLastname(request.getHeader(lastnameRequestHeader));
        setEmail(request.getHeader(emailRequestHeader));
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

















}
