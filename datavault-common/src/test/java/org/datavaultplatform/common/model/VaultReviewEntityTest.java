package org.datavaultplatform.common.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class VaultReviewEntityTest extends BaseEntityTest<VaultReview, String> {

  @Test
  void testEntity() {
    checkEntity(VaultReview.class, this::generateID);
  }
}
