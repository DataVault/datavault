package org.datavaultplatform.common.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class DepositReviewSerializationTest extends VaultReviewSerializationTest{

    @Test
    @SneakyThrows
    void testSerialization() {
        String json = mapper.writeValueAsString(dr1);
        System.out.println(json);
    }
}
