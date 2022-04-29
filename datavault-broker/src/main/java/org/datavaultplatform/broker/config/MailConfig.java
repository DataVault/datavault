package org.datavaultplatform.broker.config;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

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

}
