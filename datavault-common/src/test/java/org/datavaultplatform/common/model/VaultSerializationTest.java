package org.datavaultplatform.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * This test was added after a JSON serialization loop wasthild VaultReviews back to parent Vault.
 */
class VaultSerializationTest {

    ObjectMapper mapper;
    
    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
    }
    
    @Test
    @SneakyThrows
    void testVaultAndVaultReviewsSerialization() {
        Vault vault = new Vault();
        vault.setName("Test Vault");

        List<VaultReview> reviews = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> {
                    VaultReview vr = new VaultReview();
                    vr.setVault(vault);
                    vr.setComment("VaultReview #" + i);
                    return vr;
                })
                .toList();
        vault.setVaultReviews(reviews);
        assertThat(vault.getVaultReviews()).hasSize(5);
        String json = mapper.writeValueAsString(vault);
        System.out.println(json);
    }
    
    @Test
    @SneakyThrows
    void testVaultAndDepositsSerialization() {
        
        Vault vault = new Vault();

        List<Deposit> deposits = IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> {
                        Deposit deposit = new Deposit();
                        deposit.setVault(vault);
                        deposit.setName("Deposit #" + i);
                        return deposit;
                    })
                    .toList();
        deposits.forEach(vault::addDeposit);
        vault.setName("Test Vault");
        assertThat(vault.getDeposits()).hasSize(5);
        String json = mapper.writeValueAsString(vault);
        System.out.println(json);
    }
    
    @Test
    @SneakyThrows
    void testVaultAndDataCreatorsSerialization() {
        Vault vault = new Vault();
        List<DataCreator> dataCreators = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> {
                    DataCreator dc = new DataCreator();
                    dc.setVault(vault);
                    dc.setName("DataCreator #" + i);
                    return dc;
                })
                .toList();
        vault.setName("Test Vault");
        vault.setDataCreator(dataCreators);
        assertThat(vault.getDataCreators()).hasSize(5);
        String json = mapper.writeValueAsString(vault);
        System.out.println(json);
    }


    @Test
    @SneakyThrows
    void testVaultAndDataManagersSerialization() {
        Vault vault = new Vault();
        List<DataManager> dataManagers = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> {
                    DataManager dm = new DataManager();
                    dm.setVault(vault);
                    dm.setUUN("DataManager #" + i);
                    return dm;
                })
                .toList();
        vault.setName("Test Vault");
        dataManagers.forEach(vault::addDataManager);
        assertThat(vault.getDataManagers()).hasSize(5);
        String json = mapper.writeValueAsString(vault);
        System.out.println(json);
    }

}