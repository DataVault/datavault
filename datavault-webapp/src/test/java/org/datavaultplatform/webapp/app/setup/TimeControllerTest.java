package org.datavaultplatform.webapp.app.setup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.datavaultplatform.webapp.test.TestClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@Import(TestClockConfig.class)
@AutoConfigureMockMvc
@ProfileStandalone
public class TimeControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  void testTimeController() throws Exception {
    mvc.perform(get("/test/time")).andExpect(
        content()
            .string("{\"time\":\"2022-03-29T14:15:16.101\"}"));
  }

}
