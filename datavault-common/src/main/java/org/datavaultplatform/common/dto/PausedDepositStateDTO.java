package org.datavaultplatform.common.dto;


import org.datavaultplatform.common.model.PausedDepositState;

import java.time.LocalDateTime;

public record PausedDepositStateDTO(boolean isPaused, LocalDateTime created) {

    public PausedDepositStateDTO(PausedDepositState state) {
        this(state.isPaused(), state.getCreated());
    }

    public static PausedDepositStateDTO create(PausedDepositState state) {
        return new PausedDepositStateDTO(state.isPaused(), state.getCreated());
    }
}
