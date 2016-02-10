package org.datavaultplatform.broker.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import javax.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:23
 */

public class RestWebAuthenticationDetails extends WebAuthenticationDetails {

    private String ClientKeyRequestHeader = "X-Client-Key";

    public RestWebAuthenticationDetails(HttpServletRequest request) {
        super(request);

        setClientKeyRequestHeader(request.getHeader(ClientKeyRequestHeader));
    }

    public String getClientKeyRequestHeader() {
        return ClientKeyRequestHeader;
    }

    public void setClientKeyRequestHeader(String clientKeyRequestHeader) {
        ClientKeyRequestHeader = clientKeyRequestHeader;
    }















}
