package org.datavaultplatform.common.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
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

    public static RetrievedChunks fromJson(String retrievedChunksJson) throws JsonProcessingException {
        if (StringUtils.isBlank(retrievedChunksJson)) {
            return new RetrievedChunks();
        } else {
            RetrievedChunks result = new ObjectMapper().readValue(retrievedChunksJson, RetrievedChunks.class);
            return result;
        }
    }
    
    public static String toJson(RetrievedChunks retrievedChunks) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(retrievedChunks);
    }
}
