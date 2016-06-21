package org.datavaultplatform.webapp.authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class AuthenticationSuccess extends SavedRequestAwareAuthenticationSuccessHandler {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // Get some details from the request
        String remoteAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        CreateClientEvent clientEvent = new CreateClientEvent(remoteAddress, userAgent);
        
        // Log the authentication success with the broker
        restService.notifyLogin(clientEvent);
        
        // Check whether the user is an owner of any groups
        boolean groupOwner = false; 
        for (Group group : restService.getGroups()) {
            for (User owner : group.getOwners()) {
                if (owner.getID().equals(authentication.getName())) {
                    groupOwner = true;
                    break;
                }
            }
            if (groupOwner) {
                break;
            }
        }
        
        if (groupOwner) {
            request.getSession().setAttribute("isGroupOwner", "true");
        } else {
            request.getSession().setAttribute("isGroupOwner", "false");
        }
        
        // Continue with request processing
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
