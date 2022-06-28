package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JobEntityTest extends BaseEntityTest<Job, String> {

  @Test
  void testEntity() {
    checkEntity(Job.class, this::generateID);
  }
}
