package org.datavaultplatform.webapp.controllers;

import java.util.List;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
public class LogoutListener implements ApplicationListener<SessionDestroyedEvent> {
    
    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }
    
    @Override
    public void onApplicationEvent(SessionDestroyedEvent event)
    {        
        List<SecurityContext> contexts = event.getSecurityContexts();
        
        for (SecurityContext context : contexts)
        {
            Authentication auth = context.getAuthentication();
            
            // Get some details from the context
            WebAuthenticationDetails details = (WebAuthenticationDetails)auth.getDetails();
            String remoteAddress = details.getRemoteAddress();
            CreateClientEvent clientEvent = new CreateClientEvent(remoteAddress, null);
            
            // Log the event with the broker
            try {
                restService.notifyLogout(clientEvent);
            }catch (Exception e){
                System.err.println("Error when notifying Logout to Broker!");
            }
        }
    }
}
