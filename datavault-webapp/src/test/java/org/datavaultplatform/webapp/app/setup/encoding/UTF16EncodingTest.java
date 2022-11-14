package org.datavaultplatform.webapp.app.setup.encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "server.servlet.encoding.charset=UTF-16")
public class UTF16EncodingTest extends BaseServletEncodingTest{

  @Value("${server.servlet.encoding.charset}")
  String encoding;

  @Test
  void testProperty(){
    assertEquals("UTF-16", encoding);
  }

  @Override
  public String getEncoding() {
    return encoding;
  }
  
}
