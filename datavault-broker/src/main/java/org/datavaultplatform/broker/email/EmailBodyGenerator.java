package org.datavaultplatform.broker.email;

import java.io.StringWriter;
import java.util.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class EmailBodyGenerator {

  private final VelocityEngine velocityEngine;

  private final TemplateResolver templateResolver;

  private static final String EMAIL_ENCODING = "UTF-8";
  public EmailBodyGenerator(VelocityEngine velocityEngine, TemplateResolver templateResolver) {
    this.velocityEngine = velocityEngine;
    this.templateResolver = templateResolver;
  }

  public String generate(String templateName, Map<String,Object> model) {
      StringWriter writer = new StringWriter();
      Template template = velocityEngine.getTemplate(templateResolver.resolve(templateName), EMAIL_ENCODING);
      template.merge(new VelocityContext(model), writer);
      return writer.toString();
  }
}
