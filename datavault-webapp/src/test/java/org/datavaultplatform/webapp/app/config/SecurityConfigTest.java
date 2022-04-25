package org.datavaultplatform.webapp.app.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;
import lombok.SneakyThrows;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

@SpringBootTest
@AddTestProperties
public class SecurityConfigTest {

  @Autowired
  @Qualifier("webSecurityExpressionHandler")
  DefaultWebSecurityExpressionHandler expressionHandler;

  @Autowired
  @Qualifier("permissionEvaluator")
  PermissionEvaluator permissionEvaluator;

  @Test
  void checkExpressionHandler(ApplicationContext ctx) {
    Map<String, AbstractSecurityExpressionHandler> handlers = ctx.getBeansOfType(
        AbstractSecurityExpressionHandler.class);
    assertEquals(1, handlers.size());
    assertTrue(handlers.containsKey("webSecurityExpressionHandler"));
  }

  @Test
  @SneakyThrows
  void testEvaluatorWiring() {
    Field f = AbstractSecurityExpressionHandler.class.getDeclaredField("permissionEvaluator");
    f.setAccessible(true);
    PermissionEvaluator actualEvaluator = (PermissionEvaluator)f.get(expressionHandler);
    assertEquals(permissionEvaluator, actualEvaluator);
  }
}
