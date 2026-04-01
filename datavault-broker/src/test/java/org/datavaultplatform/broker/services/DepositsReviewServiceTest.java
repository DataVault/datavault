package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
import org.datavaultplatform.common.model.dao.DepositDAO;
import org.datavaultplatform.common.model.dao.DepositReviewDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositsReviewServiceTest {
    
    Clock clock;
    
    @Mock
    DepositReviewDAO mDepositReviewDAO;

    @Mock
    DepositDAO mDepositDAO;

    DepositsReviewService serviceSpy;
    
    LocalDateTime timestamp;
    
    @BeforeEach
    void setup() {
        Instant fixedInstant = Instant.parse("2023-10-01T10:00:00Z");
        ZoneId zone = ZoneId.of("UTC");
        clock = Clock.fixed(fixedInstant, zone);
        serviceSpy = Mockito.spy(new DepositsReviewService(mDepositReviewDAO, mDepositDAO, clock));
        timestamp = LocalDateTime.now(clock);
    }

    @Test
    void testSaveDepositReview() {

        DepositReview depositReview = new DepositReview();
        assertThat(depositReview.getCreationTime()).isNull();

        serviceSpy.saveDepositReview(depositReview);

        verify(mDepositReviewDAO).save(depositReview);
        assertThat(depositReview.getCreationTime()).isEqualTo(timestamp);
        
        verifyNoMoreInteractions(mDepositReviewDAO);
    }

    @Nested
    class TestAddDepositReviews {
        
        @Test
        void testAddDepositReviewsNullVault(){
            List<DepositReview> depositReviews = serviceSpy.addDepositReviews(null, new VaultReview());
            assertThat(depositReviews).isEmpty();
        }
        
        @Test
        void testAddDepositReviews() {

            ArgumentCaptor<DepositReview> argDepositReview = ArgumentCaptor.forClass(DepositReview.class);

            // mock the internal call to saveDepositReview - we have tested it elsewhere
            doNothing().when(serviceSpy).saveDepositReview(argDepositReview.capture());            
            
            Deposit dep1 = new Deposit();
            Deposit dep2 = null;
            Deposit dep3 = new Deposit();
            Vault vault = new Vault(){
                @Override
                public List<Deposit> getDeposits() {
                    //noinspection ConstantValue
                    return Arrays.asList(dep1, dep2, dep3);
                }
            };
            
            VaultReview vaultReview = new VaultReview();
            
            List<DepositReview> result = serviceSpy.addDepositReviews(vault, vaultReview);
            
            assertThat(result).hasSize(2);
            
            DepositReview dr1 = result.get(0);
            DepositReview dr2 = result.get(1);
            
            assertThat(dr1.getDeposit()).isEqualTo(dep1);
            assertThat(dr1.getVaultReview()).isEqualTo(vaultReview);

            assertThat(dr2.getDeposit()).isEqualTo(dep3);
            assertThat(dr2.getVaultReview()).isEqualTo(vaultReview);

            verify(serviceSpy, times(2)).saveDepositReview(any(DepositReview.class));
            
            assertThat(argDepositReview.getAllValues().get(0)).isEqualTo(dr1);
            assertThat(argDepositReview.getAllValues().get(1)).isEqualTo(dr2);
            verifyNoMoreInteractions(mDepositReviewDAO);
            
        }
    }
    
    @Test
    void testGetDepositReviews() {
        List<DepositReview> reviews = List.of(new DepositReview(), new DepositReview());
        when(mDepositReviewDAO.list()).thenReturn(reviews);

        assertThat(serviceSpy.getDepositReviews()).isEqualTo(reviews);

        verify(mDepositReviewDAO).list();
        verifyNoMoreInteractions(mDepositReviewDAO);
    }

    @Nested
    class GetDepositReviewTests {

        @Test
        void testGetDepositReviewFound(){

            DepositReview depositReview = new DepositReview();
            when(mDepositReviewDAO.findById("depositReviewId")).thenReturn(Optional.of(depositReview));

            String depositReviewId = "depositReviewId";

            DepositReview result = serviceSpy.getDepositReview(depositReviewId);
            assertThat(result).isEqualTo(depositReview);
            
            verify(mDepositReviewDAO).findById("depositReviewId");
            
            verifyNoMoreInteractions(mDepositReviewDAO);         
        }
        @Test
        void testGetDepositReviewNotFound(){

            when(mDepositReviewDAO.findById("depositReviewId")).thenReturn(Optional.empty());

            String depositReviewId = "depositReviewId";

            DepositReview result = serviceSpy.getDepositReview(depositReviewId);
            assertThat(result).isNull();

            verify(mDepositReviewDAO).findById("depositReviewId");

            verifyNoMoreInteractions(mDepositReviewDAO);
        }
    }
    

    @Test
    void testSearch(){

        String query = "query";
        
        serviceSpy.search(query);

        verify(mDepositReviewDAO).search(query);
        
        verifyNoMoreInteractions(mDepositReviewDAO);
    }

    @Test
    void testUpdateDepositReview(){

        DepositReview depositReview = new DepositReview();

        serviceSpy.updateDepositReview(depositReview);

        verify(mDepositReviewDAO).update(depositReview);
        
        verifyNoMoreInteractions(mDepositReviewDAO);
    }

    @Test
    void testCount(){
        
        when(mDepositReviewDAO.count()).thenReturn(2112L);
        
        assertThat(serviceSpy.count()).isEqualTo(2112L);
        
        verify(mDepositReviewDAO).count();
        verifyNoMoreInteractions(mDepositReviewDAO);
    }
    
    @Nested
    class RefreshDepositReviewsTests {
        Vault vault;
        Deposit deposit1;
        Deposit deposit2;
        Deposit deposit3;
        Deposit deposit4;

        @BeforeEach
        void setup() {
            vault = new Vault() {
                @Override
                public String getID() {
                    return "vaultId123";
                }
            };
            deposit1 = new Deposit() {
                @Override
                public String getID() {
                    return "depositId1";
                }
            };
            deposit2 = new Deposit() {
                @Override
                public String getID() {
                    return "depositId2";
                }
            };
            deposit3 = new Deposit() {
                @Override
                public String getID() {
                    return "depositId3";
                }
            };
            deposit4 = new Deposit() {
                @Override
                public String getID() {
                    return "depositId4";
                }
            };
        }

        @Nested
        class RefreshDepositReviewsBadArgTests {
            @Test
            void testNullVault() {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                    serviceSpy.refreshDepositReviews(null, new VaultReview());
                });
                assertThat(ex).hasMessage("The vault cannot be null");
            }
            
            @Test
            void testNoVaultId() {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                    serviceSpy.refreshDepositReviews(new Vault(), new VaultReview());
                });
                assertThat(ex).hasMessage("The vault must have an id");
            }

            @Test
            void testNullVaultReview() {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                    serviceSpy.refreshDepositReviews(vault, null);
                });
                assertThat(ex).hasMessage("The vault review cannot be null");
            }

            @Test
            void testVaultReviewIsNotUnderway() {
                VaultReview vaultReview = new VaultReview();
                vaultReview.setActionedDate(LocalDateTime.now(clock));
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                    serviceSpy.refreshDepositReviews(vault, vaultReview);
                });
                assertThat(ex).hasMessage("The vault review must be underway");
            }
        }


        @Test
        void testRefreshNoDepositReviewsAdded() {
            VaultReview vaultReview = new VaultReview();
            vaultReview.setId("vaultReviewId123");
            vaultReview.setVault(vault);
            
            DepositReview depositReview1 = new DepositReview();
            depositReview1.setVaultReview(vaultReview);
            depositReview1.setDeposit(deposit1);
            
            DepositReview depositReview2 = new DepositReview();
            depositReview2.setVaultReview(vaultReview);
            depositReview2.setDeposit(deposit2);
            
            vaultReview.setDepositReviews(new ArrayList<>(List.of(depositReview1, depositReview2)));
            
            when(mDepositDAO.getDepositsByVaultId("vaultId123")).thenReturn(Arrays.asList(deposit2, null, deposit1));
            
            List<DepositReview> result = serviceSpy.refreshDepositReviews(vault, vaultReview);
            
            assertThat(result).isEmpty();
            
            verify(mDepositDAO).getDepositsByVaultId("vaultId123");
            verifyNoMoreInteractions(mDepositDAO);
        }
        
        @Test
        void testRefreshTwoDepositReviewsAdded() {
            ArgumentCaptor<DepositReview> argDepositReview = ArgumentCaptor.forClass(DepositReview.class);
            doNothing().when(serviceSpy).saveDepositReview(argDepositReview.capture());

            VaultReview vaultReview = new VaultReview();
            vaultReview.setId("vaultReviewId123");
            vaultReview.setVault(vault);

            DepositReview depositReview1 = new DepositReview();
            depositReview1.setId("depositReviewId1");
            depositReview1.setVaultReview(vaultReview);
            depositReview1.setDeposit(deposit1);

            DepositReview depositReview2 = new DepositReview();
            depositReview2.setId("depositReviewId2");
            depositReview2.setVaultReview(vaultReview);
            depositReview2.setDeposit(deposit2);

            vaultReview.setDepositReviews(new ArrayList<>(List.of(depositReview1, depositReview2)));

            when(mDepositDAO.getDepositsByVaultId("vaultId123")).thenReturn(Arrays.asList(deposit2, null, deposit1, null, deposit3, deposit4));

            List<DepositReview> result = serviceSpy.refreshDepositReviews(vault, vaultReview);

            assertThat(result).hasSize(2);
            DepositReview depositReviewFor3 = result.stream().filter(dr -> dr.getDeposit().getID().equals("depositId3")).findFirst().orElseThrow();
            DepositReview depositReviewFor4 = result.stream().filter(dr -> dr.getDeposit().getID().equals("depositId4")).findFirst().orElseThrow();

            assertThat(depositReviewFor3.getVaultReview()).isEqualTo(vaultReview);
            assertThat(depositReviewFor3.getDeposit()).isEqualTo(deposit3);

            assertThat(depositReviewFor4.getVaultReview()).isEqualTo(vaultReview);
            assertThat(depositReviewFor4.getDeposit()).isEqualTo(deposit4);
            
            assertThat(vaultReview.getDepositReviews()).contains(depositReview1, depositReview2, depositReviewFor3, depositReviewFor4);

            verify(mDepositDAO).getDepositsByVaultId("vaultId123");
            verify(serviceSpy, times(2)).saveDepositReview(any(DepositReview.class));

            assertThat(argDepositReview.getAllValues()).containsExactlyInAnyOrder(depositReviewFor3, depositReviewFor4);

            verifyNoMoreInteractions(mDepositDAO);
        }
    }   
}