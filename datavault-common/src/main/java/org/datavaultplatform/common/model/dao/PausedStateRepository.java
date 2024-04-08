package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PausedState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PausedStateRepository extends JpaRepository<PausedState,String> {

    @Query("select PS from PausedState as PS where PS.id = (select M.id from PausedState M where M.created = (select max(created) from PausedState ))")
    Optional<PausedState> getCurrentState();

    @Query("select PS from PausedState as PS order by PS.created desc limit :limit")
    List<PausedState> getRecentEntries(int limit);

}
