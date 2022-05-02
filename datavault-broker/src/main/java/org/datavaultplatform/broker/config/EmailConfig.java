package org.datavaultplatform.broker.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;
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
public class EmailConfig {

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
  VelocityEngine velocityEngine() throws IOException {
    Properties props = new Properties();
    props.load(velocityProps.getInputStream());

    VelocityEngine engine = new VelocityEngine(props);
    engine.init();
    return engine;
  }

  @Bean
  TemplateResolver templateResolver() {
    return templateName -> Paths.get("mail-templates", templateName).toString();
  }

  @Bean
  public EmailBodyGenerator mailBodyGenerator() throws IOException {
    return new EmailBodyGenerator(velocityEngine(), templateResolver());
  }

}
