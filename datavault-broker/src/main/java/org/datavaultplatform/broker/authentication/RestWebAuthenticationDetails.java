package org.datavaultplatform.broker.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import jakarta.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:23
 */

public class RestWebAuthenticationDetails extends WebAuthenticationDetails {

    private String clientKeyRequestHeader;

    public RestWebAuthenticationDetails(HttpServletRequest request, String clientKeyRequestHeader) {
        super(request);
        setClientKeyRequestHeader(request.getHeader(clientKeyRequestHeader));
    }

    public String getClientKeyRequestHeader() {
        return clientKeyRequestHeader;
    }

    public void setClientKeyRequestHeader(String clientKeyRequestHeader) {
        this.clientKeyRequestHeader = clientKeyRequestHeader;
    }















}
