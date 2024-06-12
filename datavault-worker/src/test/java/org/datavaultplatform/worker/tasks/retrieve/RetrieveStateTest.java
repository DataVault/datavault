package org.datavaultplatform.worker.tasks.retrieve;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class RetrieveStateTest {

    @Test
    void testGetRetrieveStates(){
        ArrayList<String> states = new ArrayList<>();
        states.add("Computing free space");    // 0
        states.add("Retrieving from archive"); // 1
        states.add("Validating data");         // 2
        states.add("Transferring files");      // 3
        states.add("Data retrieve complete");  // 4
        assertThat(RetrieveState.getRetrieveStates()).isEqualTo(states);
    }

}