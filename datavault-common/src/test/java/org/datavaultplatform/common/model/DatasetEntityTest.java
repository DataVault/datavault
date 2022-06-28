package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DatasetEntityTest extends BaseEntityTest<Dataset, String> {

  @Test
  void testEntity() {
    checkEntity(Dataset.class, this::generateID);
  }
}
