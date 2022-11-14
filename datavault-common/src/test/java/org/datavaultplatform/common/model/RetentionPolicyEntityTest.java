package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class RetentionPolicyEntityTest extends BaseEntityTest<RetentionPolicy, Integer> {

  @Test
  void testEntity() {
    checkEntity(RetentionPolicy.class, this::generateIntID);
  }
}
