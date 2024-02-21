package org.datavaultplatform.worker.rabbit;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.docker.DockerImage;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@Slf4j
public abstract class BaseRabbitTCTest {

  public static final int HI_PRIORITY = 2;
  public static final int NORMAL_PRIORITY = 1;

  @Container
  @ServiceConnection
  private static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImage.RABBIT_IMAGE_NAME)
      .withExposedPorts(5672, 15672);

  @PostConstruct
  void init() {
    log.info("RABBIT HTTP URL [ {} ]", RABBIT.getHttpUrl());
    log.info("rabbit username [{}]", RABBIT.getAdminUsername());
    log.info("rabbit password [{}]", RABBIT.getAdminPassword());
    log.info("rabbit host [{}]", RABBIT.getHost());
    log.info("rabbit AMQP port [{}]", RABBIT.getAmqpPort());
  }
}
