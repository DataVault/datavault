package org.datavaultplatform.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

class VaultReviewSerializationTest {

    Clock clock;
    ObjectMapper mapper;

    protected VaultReview review;
    protected DepositReview dr1;
    protected DepositReview dr2;
    protected DepositReview dr3;
    protected DepositReview dr4;
    
    @BeforeEach
    @SneakyThrows
    void setup() {
        clock = Clock.fixed(Instant.parse("2007-12-03T10:15:30.00Z"), ZoneId.systemDefault());
        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());

        Vault vault = new Vault() {
            @Override
            public String getID() {
                return "vault-id-123";
            }
        };
        Deposit deposit1 = new Deposit();
        deposit1.setName("deposit-1");
        deposit1.setVault(vault);
        Deposit deposit2 = new Deposit();
        deposit2.setName("deposit-2");
        deposit2.setVault(vault);
        Deposit deposit3 = new Deposit();
        deposit3.setName("deposit-3");
        deposit3.setVault(vault);
        Deposit deposit4 = new Deposit();
        deposit4.setName("deposit-4");
        deposit4.setVault(vault);
        
        vault.addDeposit(deposit1);
        vault.addDeposit(deposit2);
        vault.addDeposit(deposit3);
        vault.addDeposit(deposit4);
        
        review = new VaultReview();
        review.setId("review-id-123");
        review.setComment("test comment");
        review.setVault(vault);
        review.setCreationTime(LocalDateTime.now(clock));
        review.setActionedDate(LocalDateTime.now(clock).plusDays(30));
        review.setOldReviewDate(LocalDate.now(clock).plusDays(60));

        dr1 = new DepositReview();
        dr1.setComment("dr1-comment");
        dr1.setCreationTime(LocalDateTime.now(clock));
        dr1.setActionedDate(LocalDateTime.now(clock).plusDays(11));
        dr1.setVaultReview(review);
        dr1.setDeleteStatus(DepositReviewDeleteStatus.ONREVIEW);
        dr1.setDeposit(deposit1);


        dr2 = new DepositReview();
        dr2.setComment("dr2-comment");
        dr2.setCreationTime(LocalDateTime.now(clock));
        dr2.setActionedDate(LocalDateTime.now(clock).plusDays(12));
        dr2.setVaultReview(review);
        dr2.setDeleteStatus(DepositReviewDeleteStatus.NOW);
        dr2.setDeposit(deposit2);

        dr3 = new DepositReview();
        dr3.setComment("dr2-comment");
        dr3.setCreationTime(LocalDateTime.now(clock));
        dr3.setActionedDate(LocalDateTime.now(clock).plusDays(13));
        dr3.setVaultReview(review);
        dr3.setDeleteStatus(DepositReviewDeleteStatus.ONEXPIRY);
        dr3.setDeposit(deposit3);
        
        dr4 = new DepositReview();
        dr4.setComment("dr2-comment");
        dr4.setCreationTime(LocalDateTime.now(clock));
        dr4.setActionedDate(LocalDateTime.now(clock).plusDays(13));
        dr4.setVaultReview(review);
        dr4.setDeleteStatus(DepositReviewDeleteStatus.RETAIN);
        dr4.setDeposit(deposit4);
        
        review.setDepositReviews(List.of(dr1, dr2, dr3, dr4));

    }
    

    @Test
    @SneakyThrows
    void testSerialization() {
        String json = mapper.writeValueAsString(review);
        System.out.println(json);
    }
}
