package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ClientEntityTest extends BaseEntityTest<Client, String> {

  @Test
  void testEntity() {
    checkEntity(Client.class, this::generateID);
  }
}
