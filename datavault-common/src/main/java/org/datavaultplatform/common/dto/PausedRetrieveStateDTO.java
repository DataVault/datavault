package org.datavaultplatform.common.dto;


import org.datavaultplatform.common.model.PausedRetrieveState;

import java.time.LocalDateTime;

public record PausedRetrieveStateDTO(boolean isPaused, LocalDateTime created) {

    public PausedRetrieveStateDTO(PausedRetrieveState state) {
        this(state.isPaused(), state.getCreated());
    }

    public static PausedRetrieveStateDTO create(PausedRetrieveState state) {
        return new PausedRetrieveStateDTO(state.isPaused(), state.getCreated());
    }
}
