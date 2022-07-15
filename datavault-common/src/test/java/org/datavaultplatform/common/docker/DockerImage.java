package org.datavaultplatform.common.docker;

import org.testcontainers.utility.DockerImageName;
import static org.testcontainers.utility.DockerImageName.parse;

public abstract class DockerImage {

  public static DockerImageName LDAP_IMAGE = parse("bitnami/openldap:latest");
  public static DockerImageName MYSQL_IMAGE = parse("mysql:5.7");

  public static DockerImageName MAIL_IMAGE = parse("mailhog/mailhog:v1.0.1");

  // this image has 'scp' and runs a configurable ssh daemon
  public static DockerImageName NGINX_IMAGE = parse("nginx");

  // this image has 'openssl'
  public static String OPEN_SSH_IMAGE_NAME = "linuxserver/openssh-server";

  public static DockerImageName OPEN_SSH_IMAGE = parse(OPEN_SSH_IMAGE_NAME);

}
