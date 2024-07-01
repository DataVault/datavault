package org.datavaultplatform.broker.authentication;

import org.datavaultplatform.common.util.Constants;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import jakarta.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:27
 */

public class RestWebAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    // Can be overridden in Spring config
    private String clientKeyRequestHeader = Constants.HEADER_CLIENT_KEY;

    public String getClientKeyRequestHeader() {
        return clientKeyRequestHeader;
    }

    public void setClientKeyRequestHeader(String clientKeyRequestHeader) {
        this.clientKeyRequestHeader = clientKeyRequestHeader;
    }


    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new RestWebAuthenticationDetails(context, getClientKeyRequestHeader());
    }

}
