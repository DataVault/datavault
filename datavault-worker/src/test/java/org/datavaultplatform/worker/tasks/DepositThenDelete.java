package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeleteStart;
import org.datavaultplatform.common.event.delete.DeletedChunk;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public interface DepositThenDelete {
    Logger log = LoggerFactory.getLogger(DepositThenDelete.class);

    long getArchiveCount();

    Optional<Integer> getExpectedNumberChunksPerDeposit();

    List<CompleteCopyUpload> getCopyUploadCompleteEvents();

    default List<ArchiveStoreInformation> getArchiveStoreInformation(){
        return List.of(new ArchiveStoreInformation(LocalFileSystem.class,"ARCHIVE-STORE-DST-ID"));
    }

    default void checkDeleteEvents(List<Event> events) {
        events.sort(Comparator.comparing(Event::getSequence));
        int expectedNumberOfDeletedChunkEvents = getExpectedNumberChunksPerDeposit().orElse(1) * getChunkMultiplier();
        for (int i = 0; i < events.size(); i++) {
            log.info("event[{}] {}", i, events.get(i).getClass().getName());
        }
        Set<String> uniqueDepositIds = events.stream().map(Event::getDepositId).collect(Collectors.toSet());
        assertThat(uniqueDepositIds).isEqualTo(Set.of("test-deposit-id"));

        Set<String> uniqueJobIds = events.stream().map(Event::getJobId).collect(Collectors.toSet());
        assertThat(uniqueJobIds).hasSize(1);

        int expectedNumberOfExpectedEvents = 4 + expectedNumberOfDeletedChunkEvents;
        assertThat(events).hasSize(expectedNumberOfExpectedEvents);
        assertThat(events.get(0).getClass()).isEqualTo(InitStates.class);
        assertThat(events.get(1).getClass()).isEqualTo(DeleteStart.class); // DELETE START
        assertThat(events.get(2).getClass()).isEqualTo(UpdateProgress.class); // the very first UpdateProgress
        
        List<DeletedChunk> deletedChunkEvents = new ArrayList<>();
        for (int i = 0; i < expectedNumberOfDeletedChunkEvents; i++) {
            Event event = events.get(i + 3);
            assertThat(event.getClass()).isEqualTo(DeletedChunk.class);
            DeletedChunk deletedChunk = (DeletedChunk) event;
            assertThat(deletedChunk.getMessage()).startsWith("Deleted Chunk ["+deletedChunk.getChunkNumber());
            deletedChunkEvents.add(deletedChunk);
        }

        long eventsToBeFound = (long) getNumberOfChunks() * getChunkMultiplier();
        for (ArchiveStoreInformation info : getArchiveStoreInformation()) {
            for (String location : info.locations()) {
                long found = findDeleteChunkEvents(deletedChunkEvents, info.archiveStoreType.getSimpleName(), info.archiveStoreId, location);
                assertThat(found).isEqualTo(getNumberOfChunks());
                eventsToBeFound -= found;
            }
        }
        assertThat(eventsToBeFound).isZero();
        
        Event lastEventIdx = events.get(expectedNumberOfDeletedChunkEvents + 3);
        assertThat(lastEventIdx.getClass()).isEqualTo(DeleteComplete.class); // DELETE COMPLETE

        long start = events.get(1).getTimestamp().getTime();
        long end = events.get(3).getTimestamp().getTime();

        // check that all UpdateProgres timestamps are between DeleteStart and DeleteComplete
        int firstIndexAfterDeleteStart = 2;
        int indexOfDeleteComplete = 3;
        for (int i = firstIndexAfterDeleteStart; i < indexOfDeleteComplete; i++) {
            Event event = events.get(i);
            assertThat(event.getClass()).isEqualTo(UpdateProgress.class);
            long ts = event.getTimestamp().getTime();
            assertThat(ts).isBetween(start, end);
        }
    }

    /**
     * Note : we get 1x CopyUploadComplete per archive - not per archive*location combination
     */
    default void checkDepositEvents() {
        List<CompleteCopyUpload> storedChunksEvents = getCopyUploadCompleteEvents();
        
        Function<Integer, Long> countByChunkNumber = chunkNumber -> storedChunksEvents.stream()
                .filter(copyUpload -> Objects.equals(copyUpload.getChunkNumber(), chunkNumber))
                .count();

        if (getExpectedNumberChunksPerDeposit().isEmpty()) {
            long countPerNullChunkNumber = countByChunkNumber.apply(null);
            assertThat(countPerNullChunkNumber).isEqualTo(getArchiveCount());
        } else {
            int chunksPerDeposit = getExpectedNumberChunksPerDeposit().get();
            for (int i = 0; i < chunksPerDeposit; i++) {
                int chunkNumber = i + 1;
                long countPerChunkNumber = countByChunkNumber.apply(chunkNumber);
                assertThat(countPerChunkNumber)
                        .as("Chunk number %d", chunkNumber)
                        .isEqualTo(getArchiveCount());
            }
        }
    }

    private int getChunkMultiplier() {
        return getArchiveStoreInformation().stream()
                .mapToInt(ArchiveStoreInformation::getChunkMultiplier)
                .sum();
    }

    private int getNumberOfChunks() {
        return getExpectedNumberChunksPerDeposit().orElse(1);
    }

    private long findDeleteChunkEvents(List<DeletedChunk> events, String archiveStoreType, String archiveStoreId, String location){
        return events.stream()
                .filter( dc -> dc.getMessage().contains("(%s/%s/%s".formatted(archiveStoreType,archiveStoreId,location)))
                .count();

    }

    record ArchiveStoreInformation(Class<? extends ArchiveStore> archiveStoreType, String archiveStoreId, List<String> locations) {
        public ArchiveStoreInformation(Class<? extends ArchiveStore> archiveStoreType, String archiveStoreId) {
            this(archiveStoreType, archiveStoreId, List.of(Delete.NO_LOCATION));
        }

        public int getChunkMultiplier() {
            return locations.isEmpty() ? 1 : locations.size();
        }
    }
}
