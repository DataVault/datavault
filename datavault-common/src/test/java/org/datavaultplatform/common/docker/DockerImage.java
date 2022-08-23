package org.datavaultplatform.common.docker;

import org.testcontainers.utility.DockerImageName;
import static org.testcontainers.utility.DockerImageName.parse;

public abstract class DockerImage {

  // https://hub.docker.com/r/bitnami/openldap/tags
  public static DockerImageName LDAP_IMAGE = parse("bitnami/openldap:2.6.3");

  // https://hub.docker.com/_/mysql?tab=tags
  public static DockerImageName MYSQL_IMAGE = parse("mysql:5.7.39");

  // https://hub.docker.com/r/mailhog/mailhog/tags
  public static DockerImageName MAIL_IMAGE = parse("mailhog/mailhog:v1.0.1");

  // https://hub.docker.com/_/rabbitmq?tab=tags
  public static final String RABBIT_IMAGE_NAME = "rabbitmq:3.10.7-management-alpine";

  // https://hub.docker.com/r/linuxserver/openssh-server/tags
  public static String OPEN_SSH_8pt6_IMAGE_NAME = "linuxserver/openssh-server:version-8.6_p1-r3";

  //8.8 is when they removed sha-1 signature of ssh-rsa keys - causing problems with JSch
  public static String OPEN_SSH_8pt8_IMAGE_NAME = "linuxserver/openssh-server:version-8.8_p1-r1";

  public static String OPEN_SSH_IMAGE_NAME = OPEN_SSH_8pt8_IMAGE_NAME;
  public static DockerImageName OPEN_SSH_IMAGE = parse(OPEN_SSH_IMAGE_NAME);
}
