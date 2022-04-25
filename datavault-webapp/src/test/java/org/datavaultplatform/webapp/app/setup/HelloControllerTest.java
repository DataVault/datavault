package org.datavaultplatform.webapp.app.setup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ProfileStandalone
public class HelloControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  void testHelloController() throws Exception {
    String id = "xyz123";
    MvcResult result = mvc.perform(get("/test/hello?name=" + id))
        .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(MockMvcResultMatchers.content().string(
            Matchers.containsString(id)))
        .andReturn();

    Document doc = Jsoup.parse(result.getResponse().getContentAsString());
    Element cssElem = doc.selectFirst("link");
    String cssUrl = cssElem.attr("href");
    log.info("css url", cssUrl);
    checkLink(cssUrl, "text/css;charset=UTF-8");

    Element jsElem = doc.selectFirst("script");
    String jsUrl = jsElem.attr("src");
    log.info("js url", jsUrl);
    checkLink(jsUrl,"application/javascript;charset=UTF-8");
  }

  MvcResult checkLink(String url, String contentType) throws Exception {
    log.info("url {}", url);
    return mvc.perform(get(url))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType(contentType))
        .andReturn();
  }

}
