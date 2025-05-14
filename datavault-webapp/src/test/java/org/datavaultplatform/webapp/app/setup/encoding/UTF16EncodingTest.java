package org.datavaultplatform.webapp.app.setup.encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.core.env.Environment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "server.servlet.encoding.charset=UTF-16",
        "server.servlet.encoding.force=true"
})
public class UTF16EncodingTest extends BaseServletEncodingTest{

  @Autowired
  private Environment env;

  @Test
  void testProperty(){
    assertEquals("UTF-16", env.getProperty("server.servlet.encoding.charset"));
  }

  @Override
  public String getEncoding() {
    return env.getProperty("server.servlet.encoding.charset");
  }

}
