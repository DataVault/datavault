package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.VaultReview;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositsReviewServiceTest {
    
    Clock clock;
    
    @Mock
    DepositReviewDAO mDao;
    
    DepositsReviewService serviceSpy;
    
    LocalDateTime timestamp;
    
    @BeforeEach
    void setup() {
        Instant fixedInstant = Instant.parse("2023-10-01T10:00:00Z");
        ZoneId zone = ZoneId.of("UTC");
        clock = Clock.fixed(fixedInstant, zone);
        serviceSpy = Mockito.spy(new DepositsReviewService(mDao, clock));
        timestamp = LocalDateTime.now(clock);
    }

    @Test
    void testSaveDepositReview() {

        DepositReview depositReview = new DepositReview();
        assertThat(depositReview.getCreationTime()).isNull();

        serviceSpy.saveDepositReview(depositReview);

        verify(mDao).save(depositReview);
        assertThat(depositReview.getCreationTime()).isEqualTo(timestamp);
        
        verifyNoMoreInteractions(mDao);
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
            verifyNoMoreInteractions(mDao);
            
        }
    }
    
    @Test
    void testGetDepositReviews() {
        List<DepositReview> reviews = List.of(new DepositReview(), new DepositReview());
        when(mDao.list()).thenReturn(reviews);

        assertThat(serviceSpy.getDepositReviews()).isEqualTo(reviews);

        verify(mDao).list();
        verifyNoMoreInteractions(mDao);
    }

    @Nested
    class GetDepositReviewTests {

        @Test
        void testGetDepositReviewFound(){

            DepositReview depositReview = new DepositReview();
            when(mDao.findById("depositReviewId")).thenReturn(Optional.of(depositReview));

            String depositReviewId = "depositReviewId";

            DepositReview result = serviceSpy.getDepositReview(depositReviewId);
            assertThat(result).isEqualTo(depositReview);
            
            verify(mDao).findById("depositReviewId");
            
            verifyNoMoreInteractions(mDao);         
        }
        @Test
        void testGetDepositReviewNotFound(){

            when(mDao.findById("depositReviewId")).thenReturn(Optional.empty());

            String depositReviewId = "depositReviewId";

            DepositReview result = serviceSpy.getDepositReview(depositReviewId);
            assertThat(result).isNull();

            verify(mDao).findById("depositReviewId");

            verifyNoMoreInteractions(mDao);
        }
    }
    

    @Test
    void testSearch(){

        String query = "query";
        
        serviceSpy.search(query);

        verify(mDao).search(query);
        
        verifyNoMoreInteractions(mDao);
    }

    @Test
    void testUpdateDepositReview(){

        DepositReview depositReview = new DepositReview();

        serviceSpy.updateDepositReview(depositReview);

        verify(mDao).update(depositReview);
        
        verifyNoMoreInteractions(mDao);
    }

    @Test
    void testCount(){
        
        when(mDao.count()).thenReturn(2112L);
        
        assertThat(serviceSpy.count()).isEqualTo(2112L);
        
        verify(mDao).count();
        verifyNoMoreInteractions(mDao);
    }
}