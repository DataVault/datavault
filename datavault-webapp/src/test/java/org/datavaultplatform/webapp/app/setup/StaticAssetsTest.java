package org.datavaultplatform.webapp.app.setup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@SpringBootTest
@AutoConfigureMockMvc
@ProfileStandalone
public class StaticAssetsTest {

  private static final MediaType JAVASCRIPT = MediaType.parseMediaType("application/javascript");
  private static final MediaType CSS = MediaType.parseMediaType("text/css");
  private static final MediaType ICON = MediaType.parseMediaType("image/x-icon");

  @Autowired
  MockMvc mvc;

  @Test
  void testStaticJavascript() throws Exception {

    MvcResult res = mvc.perform(get("/resources/test/js/hello.js"))
        .andExpect(content().string(Matchers.containsString("Hello World!")))
        .andReturn();

    MediaType actual = MediaType.parseMediaType(res.getResponse().getContentType());
    Assertions.assertTrue(actual.isCompatibleWith(JAVASCRIPT));
  }

  @Test
  void testStaticFavico() throws Exception {

    MvcResult res = mvc.perform(get("/resources/favicon.ico"))
        .andReturn();

    MediaType actual = MediaType.parseMediaType(res.getResponse().getContentType());
    Assertions.assertTrue(actual.isCompatibleWith(ICON));
  }

  @Test
  void testStaticCSS() throws Exception {

    MvcResult res = mvc.perform(get("/resources/test/css/hello.css"))
        .andExpect(content().string(Matchers.containsString("hello-title")))
        .andReturn();

    MediaType actual = MediaType.parseMediaType(res.getResponse().getContentType());
    Assertions.assertTrue(actual.isCompatibleWith(CSS));
  }
}
