package org.datavaultplatform.common.dto;


import org.datavaultplatform.common.model.PausedState;

import java.time.LocalDateTime;

public record PausedStateDTO(boolean isPaused, LocalDateTime created) {

    public PausedStateDTO(PausedState state) {
        this(state.isPaused(), state.getCreated());
    }

    public static PausedStateDTO create(PausedState state) {
        return new PausedStateDTO(state.isPaused(), state.getCreated());
    }
}
