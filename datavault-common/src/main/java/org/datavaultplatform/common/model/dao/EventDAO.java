package org.datavaultplatform.common.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.dao.custom.EventCustomDAO;
import org.datavaultplatform.common.util.RetrievedChunks;
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
              AND      e.timestamp >= (
                  SELECT MAX(comp.timestamp) FROM Event comp where comp.eventClass = :completeEventClass
                  AND comp.deposit.id = :depositId
                  AND e.job.taskClass = :jobTaskClass
              )
              ORDER BY e.timestamp DESC, e.sequence DESC
              LIMIT 1
            """)
    Optional<Event> findLatestDepositEventSinceLastComplete(String depositId, String jobTaskClass, String completeEventClass);

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    ((e.eventClass IS NULL) OR (e.eventClass <> 'org.datavaultplatform.common.event.Error'))
              AND      e.deposit.id = :depositId
              AND      e.job.taskClass = :jobTaskClass
              AND      NOT EXISTS (
                  SELECT comp FROM Event comp where comp.eventClass = :completeEventClass
                  AND comp.deposit.id = :depositId
                  AND e.job.taskClass = :jobTaskClass
              )
              ORDER BY e.timestamp DESC, e.sequence DESC
              LIMIT 1
            """)
    Optional<Event> findLatestEventWhereNoLastComplete(String depositId, String jobTaskClass, String completeEventClass);


    default Optional<Event> findLatestDepositEvent(String depositId) {
        return findLatestDepositEventSinceLastComplete(depositId, Job.TASK_CLASS_DEPOSIT, Complete.class.getName())
                .or(() -> findLatestEventWhereNoLastComplete(depositId, Job.TASK_CLASS_DEPOSIT, Complete.class.getName()));
    }
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

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    ((e.eventClass IS NULL) OR (e.eventClass <> 'org.datavaultplatform.common.event.Error'))
              AND      e.deposit.id = :depositId
              AND      e.job.taskClass = :jobTaskClass
              AND      e.retrieveId = :retrieveId
              AND      e.timestamp >= (
                  SELECT MAX(comp.timestamp) FROM Event comp where comp.eventClass = :completeEventClass
                  AND comp.deposit.id = :depositId
                  AND e.job.taskClass = :jobTaskClass
                  AND e.retrieveId = :retrieveId
              )
              ORDER BY e.timestamp DESC, e.sequence DESC
              LIMIT 1
            """)
    Optional<Event> findLatestRetrieveEventSinceLastComplete(String depositId, String retrieveId, String jobTaskClass, String completeEventClass);

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    ((e.eventClass IS NULL) OR (e.eventClass <> 'org.datavaultplatform.common.event.Error'))
              AND      e.deposit.id = :depositId
              AND      e.job.taskClass = :jobTaskClass
              AND      e.retrieveId = :retrieveId
              AND      NOT EXISTS (
                  SELECT comp FROM Event comp where comp.eventClass = :completeEventClass
                  AND comp.deposit.id = :depositId
                  AND e.job.taskClass = :jobTaskClass
                  AND e.retrieveId = :retrieveId
              )
              ORDER BY e.timestamp DESC, e.sequence DESC
              LIMIT 1
            """)
    Optional<Event> findLatestRetrieveEventWhereNoLastComplete(String depositId, String retrieveId, String jobTaskClass, String completeEventClass);

    default Optional<Event> findLatestRetrieveEvent(String depositId, String retrieveId) {
        return findLatestRetrieveEventSinceLastComplete(depositId, retrieveId, Job.TASK_CLASS_RETRIEVE, RetrieveComplete.class.getName())
                .or(() -> findLatestRetrieveEventWhereNoLastComplete(depositId, retrieveId, Job.TASK_CLASS_RETRIEVE, RetrieveComplete.class.getName()));
    }

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    e.eventClass = 'org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedChunk'
              AND      e.deposit.id = :depositId
              AND      e.chunkNumber IS NOT NULL
              AND      e.retrieveId = :retrieveId
              AND      NOT EXISTS (
                  SELECT comp
                  FROM   Event comp
                  WHERE  comp.eventClass = 'org.datavaultplatform.common.retrieve.RetrieveComplete'
                  AND    comp.deposit.id = :depositId
                  AND    comp.retrieveId = :retrieveId
              )
            """)
    List<Event> findEventsForRetrievedChunksWhereNoLastComplete(String depositId, String retrieveId);

    @Query("""
              SELECT   e
              FROM     Event e
              WHERE    e.eventClass = 'org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedChunk'
              AND      e.deposit.id = :depositId
              AND      e.chunkNumber IS NOT NULL
              AND      e.retrieveId = :retrieveId
              AND      e.timestamp >= (
                  SELECT MAX(comp.timestamp)
                  FROM   Event comp
                  WHERE  comp.eventClass = 'org.datavaultplatform.common.event.retrieve.RetrieveComplete'
                  AND    comp.deposit.id = :depositId
                  AND    comp.retrieveId = :retrieveId
              )
            """)
    List<Event> findEventsForRetrievedChunksSinceLastRetrieveComplete(String depositId, String retrieveId);

    default RetrievedChunks findDepositChunksRetrieved(String depositId, String retrieveId) {
        RetrievedChunks result = new RetrievedChunks();
        if (depositId != null) {
            ArrayList<Event> events = new ArrayList<>(findEventsForRetrievedChunksSinceLastRetrieveComplete(depositId, retrieveId));
            if (events.isEmpty()) {
                events.addAll(findEventsForRetrievedChunksWhereNoLastComplete(depositId, retrieveId));
            }
            result.addEvents(events);
        }
        return result;
    }
    
}
