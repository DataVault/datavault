package org.datavaultplatform.common.docker;

import org.testcontainers.utility.DockerImageName;
import static org.testcontainers.utility.DockerImageName.parse;

public abstract class DockerImage {

  // https://hub.docker.com/r/bitnami/openldap/tags
  public static final DockerImageName LDAP_IMAGE = parse("bitnami/openldap@sha256:23ebcaa52331a6521f2e512b971943a3a8b99d15459d59105d6a0eeb31220c86");

  // https://hub.docker.com/_/mariadb/tags
  public static DockerImageName MARIADB_IMAGE = parse("mariadb:10.9.4");

  // https://hub.docker.com/r/mailhog/mailhog/tags
  public static final DockerImageName MAIL_IMAGE = parse("mailhog/mailhog:v1.0.1");

  // https://hub.docker.com/_/rabbitmq?tab=tags
  public static final String RABBIT_IMAGE_NAME = "rabbitmq:3.11.3-management-alpine";

  // https://hub.docker.com/r/linuxserver/openssh-server/tags
  public static final String OPEN_SSH_8pt6_IMAGE_NAME = "linuxserver/openssh-server:version-8.6_p1-r3";

  //8.8 is when they removed sha-1 signature of ssh-rsa keys - causing problems with JSch
  public static final String OPEN_SSH_8pt8_IMAGE_NAME = "linuxserver/openssh-server:version-8.8_p1-r1";

  public static final String OPEN_SSH_9pt0_IMAGE_NAME = "linuxserver/openssh-server:version-9.0_p1-r2";

  public static final String OPEN_SSH_IMAGE_NAME = OPEN_SSH_8pt8_IMAGE_NAME;
  public static final DockerImageName OPEN_SSH_IMAGE = parse(OPEN_SSH_IMAGE_NAME);
}
