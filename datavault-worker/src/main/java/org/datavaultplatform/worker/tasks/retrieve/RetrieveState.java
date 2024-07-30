package org.datavaultplatform.worker.tasks.retrieve;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public enum RetrieveState {

    RetrieveState00ComputingFreeSpace(0,"Computing free space"),
    RetrieveState01RetrievingFromArchive(1,"Retrieving from archive"),
    RetrieveState02ValidatingData(2,"Validating data"),
    RetrieveState03TransferrinfFiles(3,"Transferring files"),
    RetrieveState04DataRetrieveComplete(4,"Data retrieve complete");
    
    private final int stateNumber;
    private final String description;
    
    RetrieveState(int stateNumber, String description){
        this.stateNumber = stateNumber;
        this.description = description;
    }
    
    public static ArrayList<String> getRetrieveStates() {
        ArrayList<String> states = new ArrayList<>();
        Arrays.stream(values()).map(RetrieveState::getDescription).forEach(states::add);
        return states;
    }
}
