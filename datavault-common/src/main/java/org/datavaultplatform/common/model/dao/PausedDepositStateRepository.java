package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PausedDepositState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PausedDepositStateRepository extends JpaRepository<PausedDepositState,String> {

    @Query("select PS from PausedDepositState as PS where PS.id = (select M.id from PausedDepositState M where M.created = (select max(created) from PausedDepositState ))")
    Optional<PausedDepositState> getCurrentState();

    @Query("select PS from PausedDepositState as PS order by PS.created desc limit :limit")
    List<PausedDepositState> getRecentEntries(int limit);

}
