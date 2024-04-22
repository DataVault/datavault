package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PausedRetrieveState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PausedRetrieveStateRepository extends JpaRepository<PausedRetrieveState,String> {

    @Query("select PS from PausedRetrieveState as PS where PS.id = (select M.id from PausedRetrieveState M where M.created = (select max(created) from PausedRetrieveState ))")
    Optional<PausedRetrieveState> getCurrentState();

    @Query("select PS from PausedRetrieveState as PS order by PS.created desc limit :limit")
    List<PausedRetrieveState> getRecentEntries(int limit);

}
