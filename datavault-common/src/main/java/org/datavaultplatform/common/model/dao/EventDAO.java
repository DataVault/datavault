package org.datavaultplatform.common.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.dao.custom.EventCustomDAO;
import org.datavaultplatform.common.util.StoredChunks;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface EventDAO extends BaseDAO<Event>, EventCustomDAO {

  @Override
  @EntityGraph(Event.EG_EVENT)
  Optional<Event> findById(String id);

  @Override
  @EntityGraph(Event.EG_EVENT)
  List<Event> findAll();

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    ((e.eventClass IS NULL) OR (e.eventClass <> 'org.datavaultplatform.common.event.Error'))
              AND      e.deposit.id = :depositId
              AND      e.job.taskClass = :jobTaskClass
              AND      NOT EXISTS (
                  SELECT comp FROM Event comp where comp.eventClass = 'org.datavaultplatform.common.event.deposit.Complete'
                  AND comp.deposit.id = :depositId
                  AND e.job.taskClass = :jobTaskClass
              )
              ORDER BY e.timestamp DESC, e.sequence DESC
              LIMIT 1
            """)
    Optional<Event> findLatestEventByDepositIdAndJobTaskClass2(String depositId, String jobTaskClass);

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    ((e.eventClass IS NULL) OR (e.eventClass <> 'org.datavaultplatform.common.event.Error'))
              AND      e.deposit.id = :depositId
              AND      e.job.taskClass = :jobTaskClass
              AND      e.timestamp >= (
                  SELECT MAX(comp.timestamp) FROM Event comp where comp.eventClass = 'org.datavaultplatform.common.event.deposit.Complete'
                  AND comp.deposit.id = :depositId
                  AND e.job.taskClass = :jobTaskClass
              )
              ORDER BY e.timestamp DESC, e.sequence DESC
              LIMIT 1
            """)
    Optional<Event> findLatestEventByDepositIdAndJobTaskClass1(String depositId, String jobTaskClass);

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    e.eventClass = 'org.datavaultplatform.common.event.deposit.CompleteCopyUpload'
              AND      e.deposit.id = :depositId
              AND      e.chunkNumber IS NOT NULL
              AND      NOT EXISTS (
                  SELECT comp
                  FROM   Event comp
                  WHERE  comp.eventClass = 'org.datavaultplatform.common.event.deposit.Complete'
                  AND    comp.deposit.id = :depositId
              )
            """)
    List<Event> findEventsForStoredChunksWhereNoLastComplete(String depositId);

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    e.eventClass = 'org.datavaultplatform.common.event.deposit.CompleteCopyUpload'
              AND      e.deposit.id = :depositId
              AND      e.chunkNumber IS NOT NULL
              AND      e.timestamp >= (
                  SELECT MAX(comp.timestamp)
                  FROM   Event comp
                  WHERE  comp.eventClass = 'org.datavaultplatform.common.event.deposit.Complete'
                  AND    comp.deposit.id = :depositId
              )
            """)
    List<Event> findEventsForStoredChunksSinceLastComplete(String depositId);

    default Optional<Event> findLatestEventByDepositIdAndJobTaskClass(String depositId, String jobTaskClass) {
        return findLatestEventByDepositIdAndJobTaskClass2(depositId, jobTaskClass)
                .or(() -> findLatestEventByDepositIdAndJobTaskClass1(depositId, jobTaskClass));
    }

    default StoredChunks findDepositChunksStored(String depositId) {
        StoredChunks result = new StoredChunks();
        if (depositId != null) {
            ArrayList<Event> events = new ArrayList<>(findEventsForStoredChunksSinceLastComplete(depositId));
            if (events.isEmpty()) {
                events.addAll(findEventsForStoredChunksWhereNoLastComplete(depositId));
            }
            result.addEvents(events);
        }
        return result;
    }
}
