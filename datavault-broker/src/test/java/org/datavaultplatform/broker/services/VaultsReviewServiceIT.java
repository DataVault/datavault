package org.datavaultplatform.broker.services;


import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.DepositReviewDAO;
import org.datavaultplatform.common.model.dao.VaultDAO;
import org.datavaultplatform.common.model.dao.VaultReviewDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@TestPropertySource(properties = {
    "broker.scheduled.enabled=false",
    "broker.rabbit.enabled=false"
})
@Import(MockRabbitConfig.class)
@Slf4j
class VaultsReviewServiceIT extends BaseDatabaseTest {
    
    @Autowired
    EntityManager em;
    
    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    private VaultsReviewService vaultsReviewService;

    @Autowired
    private VaultDAO vaultDAO;

    @Autowired
    private DepositDAO depositDAO;

    @Autowired
    private VaultReviewDAO vaultReviewDAO;

    @Autowired
    private DepositReviewDAO depositReviewDAO;

    @Autowired
    private DepositsReviewService depositReviewService;

    @Autowired
    private RolesAndPermissionsService rolesAndPermissionsService;
    
    @Autowired
    private VaultsService vaultsService;

    @MockBean
    AdminDepositService adminDepositService;

    @Autowired
    Clock clock;

    Vault vault;
    
    Deposit deposit1;
    Deposit deposit2;
    
    DepositReview depositReview1;
    DepositReview depositReview2;
    
    @Autowired
    private DepositsService depositsService;
    
    DepositReview addDepositReview(VaultReview vaultReview, Deposit deposit, String comment) {
        DepositReview depositReview = new DepositReview();
        depositReview.setDeposit(deposit);
        depositReview.setComment(comment);
        vaultReview.addDepositReview(depositReview);
        depositReviewService.saveDepositReview(depositReview);
        return depositReview;
    }

    Deposit addDeposit(String name) {
        Deposit deposit = new Deposit();
        deposit.setName("deposit2");
        deposit.setVault(vault);
        deposit.setHasPersonalData(false);
        depositsService.addDeposit(vault, deposit, "short-"+name, "origin-"+name);
        return deposit;
    }
    
    @Nested
    class RefreshDepositsOnUnderwayVaultReviewTests {
        
        @BeforeEach
        void setup() {

            assertThat(vaultsReviewService).isNotNull();
            assertThat(depositReviewService).isNotNull();
            assertThat(vaultReviewDAO).isNotNull();
            assertThat(depositReviewDAO).isNotNull();

            LocalDate today = LocalDate.now(clock);
            vault = new Vault();
            vault.setName("test-vault");
            vault.setReviewDate(today.plusYears(1));
            vault.setContact("test-contact");
            vault.setDescription("test-description");

            VaultReview vaultReview = vaultsReviewService.createVaultReview(vault);

            deposit1 = addDeposit("deposit1");

            depositReview1 = addDepositReview(vaultReview, deposit1, "comment1");

            deposit2 = addDeposit("deposit2");

            depositReview2 = addDepositReview(vaultReview, deposit2, "comment2");

            vaultsService.saveOrUpdateVault(vault);
            em.flush();
            
            assertThat(getDepositCount(vault)).isEqualTo(2);
            assertThat(getDepositReviewCount(vault)).isEqualTo(2);
        }
        
        @Test
        @Transactional
        void testNewDepositReviewsShouldBeAddedToUnderwayVaultReview() {

            Deposit deposit3 = addDeposit("deposit3");
            Deposit deposit4 = addDeposit("deposit4");
            em.flush();

            assertThat(getDepositCount(vault)).isEqualTo(4);
            assertThat(getDepositReviewCount(vault)).isEqualTo(2);

            VaultReview underwayReview1 = getUnderwayVaultReview(vault.getID());
            assertThat(underwayReview1.getDepositReviews()).hasSize(2);

            vaultsReviewService.refreshDepositsOnUnderwayVaultReview(vault.getID());
            em.flush();

            assertThat(getDepositCount(vault)).isEqualTo(4);
            //we should have added 2 new deposit reviews
            assertThat(getDepositReviewCount(vault)).isEqualTo(4);

            checkNewDepositReviewsAreForNewDeposits(List.of(deposit3, deposit4));
        }

        @Test
        @Transactional
        void testNoDepositReviewsShouldBeAddedToUnderwayVaultReview() {

            assertThat(getDepositCount(vault)).isEqualTo(2);
            assertThat(getDepositReviewCount(vault)).isEqualTo(2);

            VaultReview underwayReview1 = getUnderwayVaultReview(vault.getID());
            assertThat(underwayReview1.getDepositReviews()).hasSize(2);

            vaultsReviewService.refreshDepositsOnUnderwayVaultReview(vault.getID());
            em.flush();

            assertThat(getDepositCount(vault)).isEqualTo(2);
            //we should NOT have added 2 new deposit reviews
            assertThat(getDepositReviewCount(vault)).isEqualTo(2);

            checkNewDepositReviewsAreForNewDeposits(List.of());
        }

    }
    
    void checkNewDepositReviewsAreForNewDeposits(List<Deposit> newDeposits){
        VaultReview underwayReview2 = getUnderwayVaultReview(vault.getID());
        assertThat(underwayReview2.getDepositReviews()).hasSize(2 + newDeposits.size());
        List<String> existingDepositIds = List.of(deposit1.getID(), deposit2.getID());
        List<DepositReview> newDepositReviews = underwayReview2.getDepositReviews()
                .stream()
                .filter(dr -> !existingDepositIds.contains(dr.getDeposit().getID())).toList();
        assertThat(newDepositReviews).hasSize(newDepositReviews.size());
        Set<String> expectedNewDepositIds = newDeposits.stream().map(Deposit::getID).collect(Collectors.toSet());
        Set<String> actualNewDepositIds = newDepositReviews.stream().map(dr -> dr.getDeposit().getID()).collect(Collectors.toSet());
        assertThat(actualNewDepositIds).isEqualTo(expectedNewDepositIds);
    }

    VaultReview getUnderwayVaultReview(String vaultId) {
        List<VaultReview> allReviews = vaultsReviewService.findByVaultId(vaultId);
        assertThat(allReviews).hasSize(1);
        VaultReview underwayReview = allReviews.get(0);
        assertThat(underwayReview.isReviewUnderway()).isTrue();
        return underwayReview;
    }
    Integer getDepositCount(Vault vault) {
        Assert.notNull(vault, "Vault cannot be null");
        Assert.notNull(vault.getID(), "Vault.id cannot be null");
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Deposits WHERE vault_id = ?",
                Integer.class,
                vault.getID()
        );
    }
    
    Integer getDepositReviewCount(Vault vault) {
        Assert.notNull(vault, "Vault cannot be null");
        Assert.notNull(vault.getID(), "Vault.id cannot be null");
        return jdbcTemplate.queryForObject("""
                            SELECT COUNT(*) FROM DepositReviews dr
                                            JOIN VaultReviews vr ON dr.vaultReview_id = vr.id
                                            WHERE vr.vault_id = ?
                        """,
                Integer.class,
                vault.getID()
        );
    }
}