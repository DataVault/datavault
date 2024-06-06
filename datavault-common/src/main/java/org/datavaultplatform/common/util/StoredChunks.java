package org.datavaultplatform.common.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.springframework.util.Assert;

import java.util.*;

@Slf4j
public class StoredChunks {

    private final Map<String, Set<Integer>> storedChunksByArchiveStoredId = new HashMap<>();

    public synchronized List<Integer> getStoredChunksForArchiveStoreId(String archiveStoreId) {
        Assert.isTrue(StringUtils.isNotBlank(archiveStoreId), "The archiveStoreId cannot be blank");
        List<Integer> chunks = new ArrayList<>();
        chunks.addAll(storedChunksByArchiveStoredId.getOrDefault(archiveStoreId, Collections.emptySet()));
        Collections.sort(chunks);
        return chunks;
    }

    public synchronized void addStoredChunk(String archiveStoreId, int chunkNumber) {
        Assert.isTrue(StringUtils.isNotBlank(archiveStoreId), "The archiveStoreId cannot be blank");
        Assert.isTrue(chunkNumber > 0, "The chunkNumber must be greater than 0");
        Set<Integer> chunks = this.storedChunksByArchiveStoredId.computeIfAbsent(archiveStoreId, key -> new HashSet<>());
        chunks.add(chunkNumber);
    }

    public boolean isStored(String archiveStoreId, int chunkNumber) {
        Assert.isTrue(StringUtils.isNotBlank(archiveStoreId), "The archiveStoreId cannot be blank");
        Assert.isTrue(chunkNumber > 0, "The chunkNumber must be greater than 0");
        Set<Integer> chunks = this.storedChunksByArchiveStoredId.get(archiveStoreId);
        if (chunks == null) {
            return false;
        } else {
            return chunks.contains(chunkNumber);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @JsonProperty
    public Map<String, Set<Integer>> getStoredChunks() {
        Map<String, Set<Integer>> result = new TreeMap<>();
        for (String id : this.storedChunksByArchiveStoredId.keySet()) {
            result.put(id, new TreeSet<>(this.storedChunksByArchiveStoredId.get(id)));
        }
        return result;
    }

    public void setStoredChunks(Map<String, Set<Integer>> storedChunks) {
        if (storedChunks == null) {
            return;
        }
        for (Map.Entry<String, Set<Integer>> entry : storedChunks.entrySet()) {
            String id = entry.getKey();
            if (StringUtils.isBlank(id)) {
                continue;
            }
            Set<Integer> chunks = entry.getValue();
            if (chunks == null) {
                continue;
            }
            for (Integer chunk : chunks) {
                if (chunk == null || chunk < 1) {
                    continue;
                }
                this.addStoredChunk(id, chunk);
            }
        }
    }

    public long size() {
        return this.storedChunksByArchiveStoredId.values().stream().mapToInt(Set::size).sum();
    }

    public void addEvents(List<Event> events) {
        for (Event event : events) {
            if (event instanceof CompleteCopyUpload chunkUploadedEvent) {
                addStoredChunk(chunkUploadedEvent.getArchiveStoreId(), chunkUploadedEvent.getChunkNumber());
            }
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("storedChunks", storedChunksByArchiveStoredId)
                .toString();
    }
}
