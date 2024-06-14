package org.datavaultplatform.common.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedChunk;
import org.springframework.util.Assert;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrievedChunks {

    private Set<Integer> retrievedChunks = new HashSet<>();

    @JsonProperty
    public List<Integer> getRetrievedChunks() {
        return retrievedChunks.stream().sorted().toList();
    }

    public void setRetrievedChunks(Collection<Integer> chunks) {
        if (chunks == null) {
            return;
        }
        chunks.stream().filter(Objects::nonNull).filter(cn -> cn > 0).forEach(retrievedChunks::add);
    }

    public boolean isRetrieved(int chunkNumber) {
        Assert.isTrue(chunkNumber > 0, "The chunkNumber must be greater than 0");
        return retrievedChunks.contains(chunkNumber);
    }

    public synchronized void addRetrievedChunk(int chunkNumber) {
        Assert.isTrue(chunkNumber > 0, "The chunkNumber must be greater than 0");
         retrievedChunks.add(chunkNumber);
    }


    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public long size() {
        return this.retrievedChunks.size();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("retrievedChunks", retrievedChunks)
                .toString();
    }
    
    public void addEvents(List<Event> events) {
        for (Event event : events) {
            if (event instanceof ArchiveStoreRetrievedChunk archiveStoreRetrievedChunk) {
                addRetrievedChunk(archiveStoreRetrievedChunk.getChunkNumber());
            }
        }
    }

}
