package org.datavaultplatform.webapp.test;

import static org.awaitility.Awaitility.await;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public abstract class TestUtils {

  public static final String SET_COOKIE = "Set-Cookie";
  public static final String COOKIE = "Cookie";
  public static final Object SESSION_COOKIE = "JSESSIONID";

  private TestUtils() {
  }

  public static String getSessionId(HttpHeaders headers) {
    String setCookieValue = headers.getFirst(SET_COOKIE);
    return extractSessionId(setCookieValue);
  }

  public static String getSessionId(MvcResult result) {
    String setCookieValue = result.getResponse().getHeader(SET_COOKIE);
    return extractSessionId(setCookieValue);
  }

  /*
  The setCookie header value looks like this : 'JSESSIONID=44310C5F21C6D853C8DC8EAEAEAC6D73; Path=/; HttpOnly'
  */
  private static String extractSessionId(String setCookieValue) {
    StringTokenizer parts = new StringTokenizer(setCookieValue, ";", false);
    String part1 = parts.nextToken();
    StringTokenizer parts2 = new StringTokenizer(part1, "=", false);
    parts2.nextToken(); //SKIP OVER 'JSESSIONID' token, we want the next token, the session Id
    String sessionId = parts2.nextToken();
    return sessionId;
  }

  public static SecurityContext getSecurityContext(MvcResult result) {
    SecurityContext ctx = (SecurityContext) result.getRequest().getSession()
        .getAttribute(SPRING_SECURITY_CONTEXT_KEY);
    return ctx;
  }

  public static ResponseEntity<String> login(TestRestTemplate template, String username,
      String password) {
    return login(template.getRestTemplate(), username, password);
  }


   public static ResponseEntity<String> login(RestTemplate template, String username,
      String password) {

    CsrfInfo csrfInfo = CsrfInfo.generate(template);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    csrfInfo.addJSessionIdCookie(headers);

    LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
    params.add(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY, username);
    params.add(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY, password);
    csrfInfo.addCsrfParam(params);

    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity =
        new HttpEntity<>(params, headers);

    return template.exchange(
        "/auth/security_check",
        HttpMethod.POST,
        requestEntity,
        String.class);
  }

  public static void setSessionCookieHeader(HttpHeaders headers, String sessionId){
    headers.set(COOKIE, String.format("%s=%s;", SESSION_COOKIE, sessionId));
  }

  public static URI getFullURI(int port, String relativePath) {
    String[] parts = relativePath.split("\\?");
    String pathOnly = parts[0];
    String query = parts[1];
    UriComponents uriComponents = UriComponentsBuilder.newInstance()
        .scheme("http")
        .host("localhost")
        .port(port)
        .path(pathOnly)
        .query(query)
        .build();
    return uriComponents.toUri();
  }

  public static void waitForSessionRegistryToHaveAnEntry(SessionRegistry sessionRegistry) {
    //We have to wait for session to be added to sessionRegistry
    Callable<Boolean> ready = () -> sessionRegistry.getAllPrincipals().isEmpty() == false;

    await().atMost(5, TimeUnit.SECONDS)
        .pollInterval(100, TimeUnit.MILLISECONDS)
        .until(ready);
  }

  public static String stripPrefix(String prefix, String value){
    return value.replaceFirst("^"+prefix,"");
  }

  public static Set toSet(String... items) {
    return new HashSet(Arrays.asList(items));
  }

}
