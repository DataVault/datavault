package org.datavaultplatform.broker.queue;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.util.UsesTestContainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
@Slf4j
@UsesTestContainers
public abstract class BaseRabbitTCIT extends BaseRabbitTest {

  @Container
  @ServiceConnection
  private static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImage.RABBIT_IMAGE_NAME)
      .withExposedPorts(5672,15672);

  @PostConstruct
  void init() {
    log.info("RABBIT HTTP URL [ {} ]",RABBIT.getHttpUrl());
  }

}
