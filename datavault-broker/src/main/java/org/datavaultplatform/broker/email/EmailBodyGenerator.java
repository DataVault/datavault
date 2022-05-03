package org.datavaultplatform.broker.email;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

@Slf4j
public class EmailBodyGenerator {

  public static final String FILE_RESOURCE_LOADER_PATHS_FIELD = "paths";

  private final VelocityEngine velocityEngine;

  private final TemplateResolver templateResolver;

  private static final String EMAIL_ENCODING = "UTF-8";

  public EmailBodyGenerator(VelocityEngine velocityEngine, TemplateResolver templateResolver) {
    this.velocityEngine = velocityEngine;
    this.templateResolver = templateResolver;
  }

  public String generate(String templateName, Map<String, Object> model) {
    StringWriter writer = new StringWriter();
    String resolvedTemplateName = templateResolver.resolve(templateName);
    Template template = velocityEngine.getTemplate(resolvedTemplateName, EMAIL_ENCODING);
    getFilePath(template).ifPresent(path -> {
      //best to log if we are using an email template from an external location
      log.info("loaded[{}] from directory[{}]", template.getName(), path);
    });
    template.merge(new VelocityContext(model), writer);
    return writer.toString();
  }

  /*
   * Will get the file path of any email template loaded from an external directory
   */
  @SneakyThrows
  Optional<String> getFilePath(Template template) {
    try {
      if (template.getResourceLoader() instanceof FileResourceLoader) {
        Field fPaths = FileResourceLoader.class.getDeclaredField(FILE_RESOURCE_LOADER_PATHS_FIELD);
        fPaths.setAccessible(true);
        List<String> paths = (List<String>) fPaths.get(template.getResourceLoader());
        if (paths.isEmpty() == false) {
          return Optional.of(paths.get(0));
        }
      }
    } catch (Exception ex) {
      log.error("problem getting file path for {}", template.getName(), ex);
    }
    return Optional.empty();
  }
}
