package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class RetrieveEntityTest extends BaseEntityTest<Retrieve, String> {

  @Test
  void testEntity() {
    checkEntity(Retrieve.class, this::generateID);
  }
}
