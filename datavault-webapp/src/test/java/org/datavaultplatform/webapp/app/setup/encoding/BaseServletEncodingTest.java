package org.datavaultplatform.webapp.app.setup.encoding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.datavaultplatform.webapp.test.TestClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
@ProfileStandalone
abstract class BaseServletEncodingTest {

  @Autowired
  MockMvc mvc;

  public abstract String getEncoding();

  @Test
  void testEncoding() throws Exception {

    mvc.perform(get("/test/time"))
        .andExpect(MockMvcResultMatchers.content()
            .contentType("application/json;charset=" + getEncoding()));
  }
}
