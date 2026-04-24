package org.datavaultplatform.worker.tasks.delete;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
public enum DeleteState {

    DeleteState00DeleteStart(0, "Deleting from archive"),
    DeleteState01DeleteComplete(1, "Delete complete");

    private final int stateNumber;
    private final String description;

    DeleteState(int stateNumber, String description) {
        this.stateNumber = stateNumber;
        this.description = description;
    }

    public static ArrayList<String> getDeleteStates() {
        return Arrays.stream(values())
                .map(DeleteState::getDescription)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
