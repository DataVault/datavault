package org.datavaultplatform.webapp.test;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@Slf4j
public class CsrfInfo {
  private String sessionId;
  private String csrf;

  public static CsrfInfo generate(RestTemplate template){
    HttpEntity<String> loginPage = template.getForEntity("/auth/login",String.class);
    Document doc = Jsoup.parse(loginPage.getBody());
    String csrf = doc.selectFirst("input[type=\"hidden\"][name=\"_csrf\"]").val();
    log.info("csrf [{}]", csrf);
    String jsessionid = TestUtils.getSessionId(loginPage.getHeaders());
    return CsrfInfo.builder().csrf(csrf).sessionId(jsessionid).build();
  }

  public void addJSessionIdCookie(HttpHeaders headers){
    headers.add("Cookie", String.format("JSESSIONID=%s",sessionId));
  }

  public void addCsrfParam(MultiValueMap<String, Object> params){
    params.add("_csrf", csrf);
  }
}
