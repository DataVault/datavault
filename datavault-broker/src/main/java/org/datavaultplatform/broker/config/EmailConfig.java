package org.datavaultplatform.broker.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.datavaultplatform.broker.email.EmailBodyGenerator;
import org.datavaultplatform.broker.email.TemplateResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Slf4j
public class EmailConfig {

  public static final String EXTERNAL_EMAIL_TEMPLATE_DIR = "external.email.template.dir";

  @Value("${mail.host}")
  private String mailHost;

  @Value("${mail.port}")
  private int mailPort;

  @Value("${mail.protocol:smtp}")
  String mailProtocol;

  @Value("${mail.username}")
  private String mailUsername;

  @Value("${mail.password}")
  private String mailPassword;

  public static final String DIR_MAIL_TEMPLATES = "mail-templates";

      /*
      <bean id="mailSender" class="org.springframework.mail.javamail.JavaMailSenderImpl">
        <property name="host" value="${mail.host}"/>
        <property name="port" value="${mail.port}"/>
        <property name="protocol" value="smtp"/>
        <property name="username" value="${mail.username}"/>
        <property name="password" value="${mail.password}"/>
        <property name="defaultEncoding" value="UTF-8"/>
        <property name="javaMailProperties">
            <props>
                <prop key="mail.smtp.auth">true</prop>
                <prop key="mail.smtp.starttls.enable">true</prop>
                <prop key="mail.smtp.quitwait">false</prop>
            </props>
        </property>
    </bean>
    */

  @Bean
  @ConfigurationProperties(prefix = "jmail")
  Properties javaMailProperties() {
    return new Properties();
  }

  @Bean
  JavaMailSender mailSender() {
    JavaMailSenderImpl result = new JavaMailSenderImpl();
    result.setHost(mailHost);
    result.setPort(mailPort);
    result.setProtocol(mailProtocol);
    result.setUsername(mailUsername);
    result.setPassword(mailPassword);
    result.setDefaultEncoding(StandardCharsets.UTF_8.name());

    result.setJavaMailProperties(javaMailProperties());
    return result;
  }

  @Value("classpath:velocity.properties")
  Resource velocityProps;

  @Bean
  VelocityEngine velocityEngine(
      @Value("${external.email.template.dir:#{null}}") String externalMailTemplateDir)
      throws IOException {
    log.info("{}[]", EXTERNAL_EMAIL_TEMPLATE_DIR, externalMailTemplateDir);
    Properties props = new Properties();
    props.load(velocityProps.getInputStream());

    //If we have external directory, add extra velocity config
    Properties external = getPropertiesForExternal(externalMailTemplateDir);
    if (external != null) {
      props.putAll(external);
    }

    VelocityEngine engine = new VelocityEngine(props);
    engine.init();
    return engine;
  }

  private Properties getPropertiesForExternal(String externalTemplateDirName) {
    if (externalTemplateDirName == null) {
      return null;
    }
    File externalTemplateDir = new File(externalTemplateDirName);
    boolean okay = externalTemplateDir.exists() && externalTemplateDir.isDirectory()
        && externalTemplateDir.canRead();
    if (!okay) {
      log.error(
          "problem with external template directory {}. Either does NOT exist or is NOT a directory or is NOT readable",
          externalTemplateDirName);
      return null;
    }
    Properties external = new Properties();
    //Template files in the external directory will take precedence over classpath files
    external.put("resource.loaders", "file,classpath");
    external.put("resource.loader.file.class", FileResourceLoader.class.getName());
    external.put("resource.loader.file.path", externalTemplateDir.getAbsolutePath());
    //We don't want to cache these - changes will be reflected straight away
    external.put("resource.loader.file.cache", "false");
    return external;
  }

  @Bean
  TemplateResolver templateResolver() {
    return templateName -> Paths.get(DIR_MAIL_TEMPLATES, templateName).toString();
  }

  @Bean
  public EmailBodyGenerator mailBodyGenerator(VelocityEngine velocityEngine) {
    return new EmailBodyGenerator(velocityEngine, templateResolver());
  }

}
