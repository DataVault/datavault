package org.datavaultplatform.worker.tasks.deposit;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public enum DepositState {

    DepositState00CalculatingSize(0,"Calculating size"),
    DepositState01Transferring(1,"Transferring"),
    DepositState02Packaging(2,"Packaging"),
    DepositState03StoringInArchive(3,"Storing in archive"),
    DepositState04Verifying(4,"Verifying"),
    DepositState05Complete(5,"Complete");
    
    private final int stateNumber;
    private final String description;
    
    DepositState(int stateNumber, String description){
        this.stateNumber = stateNumber;
        this.description = description;
    }
    
    public static ArrayList<String> getDepositStates() {
        ArrayList<String> states = new ArrayList<>();
        Arrays.stream(values()).map(DepositState::getDescription).forEach(states::add);
        return states;
    }
}
