package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class DataCreatorEntityTest extends BaseEntityTest<DataCreator, String> {

  @Test
  void testEntity() {
    checkEntity(DataCreator.class, this::generateID);
  }
}
