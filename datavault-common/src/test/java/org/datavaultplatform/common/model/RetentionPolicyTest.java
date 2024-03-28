package org.datavaultplatform.common.model;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RetentionPolicyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String RETENTION_POLICY_JSON_ARRAY = "[{\"id\": 1, \"minRetentionPeriod\": 111},{\"id\": 2, \"minRetentionPeriod\": 222}]";

    @Test
    void testGetPolicyInfo() {
        RetentionPolicy rp1 = new RetentionPolicy();
        String policyInfo1 = rp1.getPolicyInfo();
        assertThat(policyInfo1).isEqualTo("null-0");

        RetentionPolicy rp2 = new RetentionPolicy();
        rp2.setId(123);
        assertThat(rp2.getPolicyInfo()).isEqualTo("123-0");

        RetentionPolicy rp3 = new RetentionPolicy();
        rp3.setId(234);
        rp3.setMinRetentionPeriod(2112);
        assertThat(rp3.getPolicyInfo()).isEqualTo("234-2112");
    }

    @Test
    @SneakyThrows
    void testSerialization() {
        RetentionPolicy rp1 = new RetentionPolicy();
        rp1.setId(1);
        rp1.setMinRetentionPeriod(111);
        RetentionPolicy rp2 = new RetentionPolicy();
        rp2.setId(2);
        rp2.setMinRetentionPeriod(222);

        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(Arrays.asList(rp1, rp2));
        System.out.println(json);
        JSONAssert.assertEquals(RETENTION_POLICY_JSON_ARRAY, json, JSONCompareMode.LENIENT);

    }

    @Test
    @SneakyThrows
    void testDeSerialization() {
        RetentionPolicy[] retentionPolicies = MAPPER.readValue(RETENTION_POLICY_JSON_ARRAY, RetentionPolicy[].class);
        assertThat(retentionPolicies).hasSize(2);
        RetentionPolicy retentionPolicy1 = retentionPolicies[0];
        assertThat(retentionPolicy1.getID()).isEqualTo(1);
        assertThat(retentionPolicy1.getMinRetentionPeriod()).isEqualTo(111);
        RetentionPolicy retentionPolicy2 = retentionPolicies[1];
        assertThat(retentionPolicy2.getID()).isEqualTo(2);
        assertThat(retentionPolicy2.getMinRetentionPeriod()).isEqualTo(222);
    }
}