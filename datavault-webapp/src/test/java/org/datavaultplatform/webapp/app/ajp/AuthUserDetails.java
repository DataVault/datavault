package org.datavaultplatform.webapp.app.ajp;

import java.util.List;

import lombok.Data;
import org.datavaultplatform.webapp.authentication.shib.ShibWebAuthenticationDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

@Data
public class AuthUserDetails {

  private final String principal;
  private final String email;
  private final String firstname;
  private final String lastname;

  private final List<String> grantedAuthorities;

  public AuthUserDetails(PreAuthenticatedAuthenticationToken authentication){
    this.principal = String.valueOf(authentication.getPrincipal());
    ShibWebAuthenticationDetails details = (ShibWebAuthenticationDetails)authentication.getDetails();
    this.email = details.getEmail();
    this.firstname = details.getFirstname();
    this.lastname = details.getLastname();
    this.grantedAuthorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted().toList();
  }
}
