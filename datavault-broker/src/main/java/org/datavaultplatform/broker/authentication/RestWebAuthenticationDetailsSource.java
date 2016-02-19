package org.datavaultplatform.broker.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * User: Robin Taylor
 * Date: 04/02/2016
 * Time: 10:27
 */

public class RestWebAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new RestWebAuthenticationDetails(context);
    }

}
