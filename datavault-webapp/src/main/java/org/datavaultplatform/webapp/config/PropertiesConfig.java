package org.datavaultplatform.webapp.config;

import java.util.Arrays;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

@Configuration
@PropertySources({
    // application.properties and application-{profile}.properties are added by default

    //allows us to log the git.commit.id.abbrev property on startup!
    @PropertySource(
            value="classpath:git.properties",
            ignoreResourceNotFound = true),

    @PropertySource(
        value = "file://${DATAVAULT_HOME}/config/datavault.properties",
        ignoreResourceNotFound = true),

    @PropertySource(
        value = "file://${DATAVAULT_ETC:/etc}/datavault/datavault.properties",
        ignoreResourceNotFound = true),

    @PropertySource(
        value = "file://${HOME}/.config/datavault/datavault.properties",
        ignoreResourceNotFound = true)
})
@Slf4j
public class PropertiesConfig {

  @Autowired
  private Environment env;

  @PostConstruct
  void init() {
    Arrays.asList("HOME", "DATAVAULT_HOME", "DATAVAULT_ETC").forEach(
        name ->
            log.info("ENV VARIABLE [{}]=[{}]", name, env.getProperty(name))
    );

  }
}
